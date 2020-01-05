# Usando JIB

Actualizar las propiedades del plugin jib con los datos del repositorio, el usuario y la contraseña:

```yml
<plugin>
<groupId>com.google.cloud.tools</groupId>
<artifactId>jib-maven-plugin</artifactId>
<version>1.7.0</version>
<configuration>
  <from>
	<image>openjdk:8u212-jre-alpine</image>
	<auth>
		<username>egsmartin</username>
		<password>Vera1511</password>
	</auth>
  </from>

  <to>
	<image>pruebacontenedor.azurecr.io/${project.artifactId}:${project.version}</image>  
	<auth>
		<username>pruebacontenedor</username>
		<password>Pr0bDGtfdIKbWj+pGbGEsFpc8D/3enAH</password>
	</auth>
  </to>
```

Podemos personalizar como la imagen va a ser creada. Por ejemplo, podriamos especificar el puerto que la imagen va a exponer - en este caso el `8085`:

```yml
	  <container>
		<ports>
			<port>8085</port>
		</ports>
		<!-- 
		<jvmFlags>
			<jvmFlag>-Xms512m</jvmFlag>
			<jvmFlag>-Xmx512m</jvmFlag>
		</jvmFlags>
		 -->
	  </container>
	  <allowInsecureRegistries>true</allowInsecureRegistries>
 </configuration>
```
			
Lanzamos el proceso de creacion de la imagen como sigue:

```sh
mvn compile jib:build
```

# Con Dockerfile

Tenemos que arrancar el demonio de docker. Una vez arrancado podemos proceder a construir la imagen con nuestro `dockerfile`.

```sh
docker build -t knative-feheroes .
```

Vamos a añadirle el tag de nuestro repositorio en Azure:

```sh
docker tag knative-feheroes:latest pruebacontenedor.azurecr.io/knative-feheroes:latest
```

Notese que aparece nuestra imagen con el tag, sigue figurando la imagen sin el tag, pero ambas tienen el __mismo hash__.

Publicamos nuestro repositorio en el repositorio de Azure

```sh
docker push pruebacontenedor.azurecr.io/knative-feheroes
```