#!/bin/bash -e

registry_url='intranet.azurecr.io'
registry_name='intranet'

# build local Dockerfile and push image to ACR as by the Jenkinsfile

pushd $(dirname $0) > /dev/null # enter current script directory
while [ ! -d .git ]; do cd ..; [ $PWD == '/' ] && exit 1; done # go back to project home

{ # try

# fails if any untracked files
[[ -z $(git status -s) ]] || {
    read -rp "Detected un-tracked files. Continue anyway? [y/N] "
    [[ ${REPLY} =~ ^[yY] ]] || { echo "Aborted."; exit 1; }
}

# fails if any unpushed commits
local_branch=$(git rev-parse --abbrev-ref HEAD)
[[ -z $(git log origin/${local_branch}..HEAD) ]] || {
    read -rp "Detected un-pushed commits on '${local_branch}'. Continue anyway? [y/N] "
    [[ ${REPLY} =~ ^[yY] ]] || { echo "Aborted."; exit 1; }
}

group_artifact_version=$(mvn -q --non-recursive exec:exec -Dexec.executable=echo -Dexec.args='${project.groupId}:${project.artifactId}:${project.version}')
group_id=$(echo $group_artifact_version | cut -d':' -f1)
artifact_id=$(echo $group_artifact_version | cut -d':' -f2)
version=$(echo $group_artifact_version | cut -d':' -f3)
commit_hash=$(git rev-parse HEAD | cut -c 1-12)

## ------ naming convention for docker image ------

# docker image naming convention ==> {dockerRegistryUrl}/{groupId}/{artifactId}:{version}-{commitHash}
#image="${registry_url}/${group_id}/${artifact_id}"
#tag="${version}-${commit_hash}"

# docker image naming convention ==> {dockerRegistryUrl}/{groupId}/{artifactId}/{version}:{commitHash}
image="${registry_url}/${group_id}/${artifact_id}/${version}"
tag="${commit_hash}"

## ------ naming convention for docker image ------

# A image repository filename must be valid ASCII and may contain lowercase letters, digits, underscores, periods and dashes
image=$(echo $image | tr '[A-Z]' '[a-z]')

# A tag filename must be valid ASCII and may contain lowercase and uppercase letters, digits, underscores, periods and dashes
tag=$(echo $tag | sed 's/[^a-zA-Z0-9_.-]\{1,\}/-/g')


# checks if image is already build locally
image_count=$(docker images $image:$tag | sed '1d' | wc -l)
if [ $image_count -eq 0 ]
then

    mvn clean -U package -DskipTests

    # build images with tag latest
    docker build -t "$image" \
        --label "created=$(date '+%Y-%m-%dT%H:%M:%S%z')" \
        --label "groupId=${group_id}" \
        --label "artifactId=${artifact_id}" \
        --label "version=${version}" \
        --label "gitBranch=$(git rev-parse --abbrev-ref HEAD)" \
        --label "gitOrigin=$(git remote get-url origin)" \
        --label "buildTag=NA" \
        -f ./Dockerfile . || { echo >&2 "docker build failed"; exit 1; }

    # re-tag images with specific tag
    docker tag "$image" "$image:$tag"

fi

read -rp "Push image to acr '${registry_url}'? [y/N] "
[[ ${REPLY} =~ ^[yY] ]] && {

echo "pushing image to acr ..."
command -v az >/dev/null 2>&1 || { echo >&2 "missing azure-cli on local machine"; exit 1; }
# set proper subscription
intranet_subscription_id=$(az account list | jq -r '.[] | select(.name == "Intranet") | .id')
az account set --subscription ${intranet_subscription_id} > /dev/null
# login to acr
az acr login -n ${registry_name} || { echo >&2 "login failed"; exit 1; }
# push image
docker push "$image:$tag" || { echo >&2 "docker push failed"; exit 1; }
docker push "$image:latest" || { echo >&2 "docker push failed"; exit 1; }

}

echo
echo "$image:$tag"
echo

} || { # catch

    echo "Error $?"
}

popd > /dev/null
