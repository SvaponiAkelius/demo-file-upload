# demo-file-upload

Demo app for file upload.


### Upload

```bash
path=$(curl -sX POST 'https://demo-file-upload.int-d.dev.k8s.azure.akelius.com' --form "file=@/your/file")
```

### Download

```bash
curl -sX GET "https://demo-file-upload.int-d.dev.k8s.azure.akelius.com?path=${path}"
```

### List

```bash
curl -sX GET "https://demo-file-upload.int-d.dev.k8s.azure.akelius.com"
```
