# TP Cripto - 1Q2022

- Trabajo Práctico de Criptografia y Seguridad - 1Q2022
- Implementación en Java
- Esteganografía

## Compilación y Ejecución del programa:

Desde la carpeta del proyecto, ejecutar:

```
$ mvn clean install
```

Para generar un "Build":

```
$ mvn pre-clean

$ mvn compile
    
$ mvn package
```

## Parametros del programa:

### Para el ocultamiento de un archivo en un .bmp ###

> -embed
- Indica que se va a ocultar información.

> -in file
- Archivo que se va a ocultar.

> -p bitmapfile
- Archivo bmp que será el portador.

> -out bitmapfile
- Archivo bmp de salida, es decir, el archivo bitmapfile con la información de file
incrustada.

> -steg <LSB1 | LSB4 | LSBI>
- Algoritmo de esteganografiado: LSB de 1bit, LSB de 4 bits, LSB Enhanced

------

- Y los siguientes parámetros opcionales:

> -a <aes128 | aes192 | aes256 | des>
> 
> -m <ecb | cfb | ofb | cbc
> 
> -pass password (password de encripcion)

#### Ejemplo de uso ####

```
$ stegobmp -embed –in “mensaje1.txt” –p “imagen1.bmp” -out “imagenmas1.bmp” –steg LSBI –a des –m cbc -pass “oculto”
```

### Para extraer de un archivo .bmp un archivo oculto ###

> -extract
- Indica que se va a extraer información.

> -p bitmapfile
- Archivo bmp portador

> -out file
- Archivo de salida obtenido

> -steg <LSB1 | LSB4 | LSBI>
- Algoritmo de esteganografiado: LSB de 1bit, LSB de 4 bits, LSB Improved

------

Y los siguientes parámetros opcionales:

> -a <aes128 | aes192 | aes256 | des>
> 
> -m <ecb | cfb | ofb | cbc>
>
> -pass password (password de encripcion)

#### Ejemplo de uso ####

```
$stegobmp –extract –p “imagenmas1 .bmp” -out “mensaje1” –steg LSBI –a des –m cbc -pass “oculto”
```


## Integrantes
- Ignacio Villanueva - 59000
- Tomas Cerdeira - 60051
- Santiago Garcia Montagner - 60352

