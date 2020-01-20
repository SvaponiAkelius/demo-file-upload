# aliases for docker image

alias e='env|sort'
alias l='ls -al'
alias ll='ls -FlAhp'
alias log='tail -n999 -f $(date +%Y-%m-%d).log'
alias run='/usr/bin/java $JAVA_OPTIONS -jar -Dserver.port=8080 ~/server.jar &'

ACTUATOR=http://localhost:8080/actuator

function hostname() { echo ${HOSTNAME}; }
function port_local() {
    [ -z $1 ] && { >&2 echo 'missing port'; return; }
    (lsof "-i:$1") 2> /dev/null || echo "Nothing running on port $1"
}
function port_remote() {
    # nc -vz [host] [port]
    # echo > /dev/tcp/[host]/[port]
    [ -z $1 ] && { >&2 echo 'missing host'; return; }
    [ -z $2 ] && { >&2 echo 'missing port'; return; }
    command -v ncs > /dev/null && {
        nc -vz $1 $2
    } || {
        echo > /dev/tcp/$1/$2 && echo "TPC port $1:$2 is open"
    }
}
function actuator_health() { curl -sSL ${ACTUATOR}/health; }
function actuator_info() { curl -sSL ${ACTUATOR}/info; }
function actuator_ready() { curl -sSL ${ACTUATOR}/ready; }
function actuator_alive() { curl -sSL ${ACTUATOR}/alive; }
function actuator_refresh() {
    read -rp "Refresh? [y/N] "
    [[ ${REPLY} =~ ^[yY] ]] && {
        curl -sSX POST ${ACTUATOR}/refresh
    }
}
function actuator_properties() {
    if [[ "$1" =~ ^(-h|--help)$ ]]; then
        echo "Usage:"
        echo "    properties                            prints all property sources (note: this may print the same property multiple times, ping point single property to see actual value)"
        echo "    properties prop_name                  (answer N) prints single property value"
        echo "    properties prop_name                  (answer Y) deletes single property from environment, like unix unset. EnvironmentChangeEvent is emitted"
        echo "    properties prop_name prop_value       creates or overwrites a single property value in the environment. EnvironmentChangeEvent is emitted"
    elif [ -z $1 ] && [ -z $2 ]; then
        # print all property sources (note: this may print the same property multiple times, ping point single property to see actual value)
        curl -sS ${ACTUATOR}/env | jq -r '.propertySources[] | "# " + .name, (.properties | to_entries[] | .key + "=" + (.value.value|tostring))'
    elif [ -z $2 ]; then
        # print all property sources (note: this may print the same property multiple times, ping point single property to see actual value)
        read -rp "Remove '$1' from environment? [y/N] "
        [[ ${REPLY} =~ ^[yY] ]] && {
            data="{\"name\": \"$1\"}"
            echo "DELETE /actuator/env"
            echo "${data}"
            curl -sSX DELETE ${ACTUATOR}/env -H 'Content-Type: application/json' -d "${data}"
        } || {
            curl -sS ${ACTUATOR}/env/${1} | jq -r ".property.value"
        }
    else
        data="{\"name\": \"$1\", \"value\": \"$2\"}"
        echo "POST /actuator/env"
        echo "${data}"
        curl -sSX POST ${ACTUATOR}/env -H 'Content-Type: application/json' -d "${data}"
    fi
}
