# Registry
Para conectarnos al registry privado creado en Azure:  

```
docker login egsmcontenedor.azurecr.io
```

Las credenciales a utilizar las podemos tomar desde el portal de azure. El usuario sera `egsmContenedor`, y la contraseña `LNTuvGUKsCf9w5lMgJwYBjiMWJL/19Ea`.  
Podemos hacer un push de nuestra imagen desde el registry local, al registry privado en Azure:  

```
docker pull www.gz.com:5000/fehello-jib:0.0.1-SNAPSHOT

docker tag www.gz.com:5000/fehello-jib:0.0.1-SNAPSHOT egsmcontenedor.azurecr.io/fehello-jib:0.0.1-SNAPSHOT

docker push egsmcontenedor.azurecr.io/fehello-jib:0.0.1-SNAPSHOT
```

# Kubectl (windows)
To configure kubectl to manage the Azure Kubernetes cluster:  

```
az aks get-credentials --resource-group miKubernetes --name miKCluster
```

# Dashboard
Abrimos el Dashboard:  

```
az aks browse --resource-group miKubernetes --name miKCluster
```

Para no bloquear la consola:  

```
start-job -ScriptBlock {az aks browse --resource-group miKubernetes --name miKCluster}
```

Para dar permisos al usuario usado por kubectl:  

```
kubectl create clusterrolebinding kubernetes-dashboard --clusterrole=cluster-admin --serviceaccount=kube-system:kubernetes-dashboard
```
