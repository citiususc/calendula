![](https://tec.citius.usc.es/calendula/github-assets/calendula_promo_google_play.png)
# Calendula

Calendula es un asistente en Android para la gestión de medicación personal. Está dirigido a aquellos que tienen problemas en seguir su régimen de medicación, se olvidan de tomar las pastillas o tienen horarios difíciles de recordar.

La aplicación está disponible para su descarga en Google Play, F-Droid y Github. 

<table>
    <tr>
        <td align="center"><a href="https://play.google.com/store/apps/details?id=es.usc.citius.servando.calendula"><img src="https://play.google.com/intl/en_us/badges/images/badge_new.png" alt="Get it on Google Play" ></a></td>
        <td align="center"><a href="https://f-droid.org/packages/es.usc.citius.servando.calendula/"><img src="https://gitlab.com/fdroid/artwork/raw/master/badge/get-it-on.png" alt="Get it on F-Droid" height="68"></a></td>
        <td align="center"><a href="https://github.com/citiususc/calendula/releases/latest"><img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Get it on Github" height="68"></a></td>
    </tr>
</table>

Visite nuestra página web para más información [https://citius.usc.es/calendula/](https://citius.usc.es/calendula/)

## Novedades de Caledula

¡Tenemos buenas noticias!

El Servicio Gallego de Salud (SERGAS) ha adoptado Calendula para ser conectada con su Sistema de Receta Electrónica. Esto ha dado lugar a varias extensiones a partir de la versión libre, incluyendo: 

* Descarga automática de la pauta de medicación del paciente.
* Acceso automático al calendario de recogida de medicaciones.
* Recomendación automática de las mejores fechas de recogida, para optimizar las visitas a la farmacia. 
* Acceso al régimen de dosis de anticoagulantes.

El proceso de integración se ha focalizado en dos aspectos fundamentales: interoperabilidad y seguridad.

Con respecto a la interoperabilidad, la adopción del estándar internacional HL7-FHIR facilitará la integración de caléndula con otros sistemas de salud.

Con respecto a la seguridad, se ha adoptado la especificación OpenID Connect, lo que permite a Calendula verifiar la identidad de los usuarios a partir de una autenticación realizada por servidores del SERGAS.

A lo largo de este proceso, hemos trabajado en muchas mejoras, incluyendo: 
* Incrementar el nivel de API Android de desarrollo al 29: 
* Adaptar las notificaciones y servicios en segundo plano a los requisitos de las nuevas versiones de Android.
* Actualizar versiones de Gradle, Java y Kotlin.
* Migrar las librerías Android a AndroidX
* Actualizar las versiones de las librerías: Google, Iconics, Material-Drawer, Fast Adapter, ButterLnife, Caldroid y Android Jobs.
* Mejoras en la UI para solucionar fallos de actualización y NPEs.
* Actualización de internacionalización: añadir nuevos idiomas y actualizar los soportados actualmente.
* Mejoras de rendimiento en el uso de memoria: migrar recursos gráficos de tipo bitmap a formato vectorial.
* Notificaciones y ahorro de batería: se añade la opción de excluir la aplicación del sistema de ahorro de batería para evitar que el sistema suspenda la aplicación e impida la entrega notificaciones al usuario.
* Corrección de fallos menores.

Estos cambios pronto estarán disponibles en este repositorio y se aplicarán a la versión de Google Play!

## Empezando

Estas instrucciones le permitirán obtener una copia del proyecto listo para usar en su máquina local. Si quiere contribuir al desarrollo de la aplicación, por favor diríjase a la sección Contribuir. 

### Preparación del entorno de desarollo

Para desarrollo, utilizamos [Android Studio](https://developer.android.com/studio/index.html) (el IDE oficial de Android), así que le recomendamos que lo utilice en su entorno de desarrollo. Una vez instalado Android Studio, puede utilizar el Android SDK Manager para obtener las herramientas, plataformas y otros componentes que necesitará para comenzar el desarrollo. Las más importantes son:

* Android SDK Tools y Android SDK Platform-tools (actualizar a la última versión suele ser una buena idea).
* Android SDK Build-Tools 27.0.3.
* Android 8.1 (API Level 27) SDK Platform.
* Android Support Repository

También puede instalar otros paquetes como emuladores para ejecutar la aplicación, si no dispone o no quiere utilizar un dispositivo hardware. La mínima versión de Android soportada es *4.1, Jelly Bean (API level 16).*

### Compilando e instalando la aplicación

Antes de nada necesita el código fuente, así que proceda a clonar este repositorio en su máquina local:

```bash
git clone https://github.com/citiususc/calendula.git
cd calendula
```

Android Studio utiliza Gradle como el componente principal del mecanismo de compilación, pero no es necesario instalarlo por separado. En vez de eso, puede utilizar el ya incluido [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). Para compilar la aplicación, abra un terminal en la carpeta del repositorio y ejecute:

```bash
./gradlew clean assembleDevelopDebug
```
*Nota: "developDebug" es la [build variant](https://developer.android.com/studio/build/build-variants.html) que utilizamos para desarrollo. Para otras variantes, por favor revise el archivo `Calendula/build.gradle`.*

Después, podrá instalar la aplicación en un dispositivo o emulador: 

```bash
adb install Calendula/build/apk/develop/debug/developDebug-[version].apk
```
Estas tareas también se pueden ejecutar desde Android Studio con algunos clicks. 

## Versiones de la aplicación

Actualmente se mantienen lanzamientos de Calendula en Google Play, F-Droid y Github.

 * La última versión de la app disponible en esas páginas refleja el código de la rama `master`.
 * Las ramas de lanzamiento se distribuyen normalmente a través del *Google Play BETA channel* antes de hacerlas públicas. Si quiere ser un miembro de la comunidad de pruebas, únase al grupo de pruebas en Google Groups y automáticamente recibirá las actualizaciones del canal BETA como cualquier otra actualización de Google Play. 
 
> Unirse al canal BETA: [¡haga click aquí!](https://play.google.com/apps/testing/es.usc.citius.servando.calendula)

Revise las [normas de contribución](CONTRIBUTING.md) para más información acerca del modelo de ramas.

## ¿Qué pinta tiene?

Intentamos seguir los principios de [Material Design](https://material.google.com/#). ¿Qué le parece el resultado?

  | <img src="https://tec.citius.usc.es/calendula/github-assets/home.png" width="230px"/>  | <img src="https://tec.citius.usc.es/calendula/github-assets/agenda.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/schedules.png" width="230px"/>
  |:---:|:---:|:---:|
  | <img src="https://tec.citius.usc.es/calendula/github-assets/aviso.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/navdrawer.png" width="230px"/> | <img src="https://tec.citius.usc.es/calendula/github-assets/profile.png" width="230px"/>

## Trabajo futuro

Tenemos una gran cantidad de ideas para nuevos desarrollos y también estamos abiertos a otras nuevas. Las siguientes nuevas características podrían ser útiles:

* Información de farmacias cercanas, su localización y horarios. 
* Asistente de viaje ¿cuántas pastillas necesito para este fin de semana?)
* Introducir conceptos de [ludificación](https://en.wikipedia.org/wiki/Gamification) para mejorar la adherencia terapéutica. 

## Atribución de diseño

Actualmente estamos utilizando los siguientes recursos en la app:

* [People Vector Pack](http://www.freepik.com/free-vector/people-avatars_761436.htm) por [Freepik](http://www.freepik.com)
* Iconos [Baby](http://www.flaticon.com/free-icon/baby_136272), [Dog](http://www.flaticon.com/free-icon/dog_194178) y [cat](http://www.flaticon.com/free-icon/cat_194179) por <a href="https://www.flaticon.com/" title="Flaticon">Flaticon</a> (<a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>)
* [Animación de Despertador](https://dribbble.com/shots/1114887-Alarm-Clock-GIF) por  [Daan De Deckere](http://daandd.be/)

## Contribuir

Puede hacer un fork y enviar un pull request si quiere contribuir al proyecto. Calendula sigue los términos de licencia de la [GNU General Public License (v3)](LICENSE.md), así que al subir contenidos al repositorio estará liberando su trabajo según esta licencia. 

Antes de empezar, revise nuestras [normas de contribución](CONTRIBUTING.md).

### Quiero contribuir, pero no soy desarrollador...

Si no es un desarrollador pero quiere ayudar, ¡no se preocupe! Puede ayudarnos a [traducir la aplicación](CONTRIBUTING.md#help-with-app-translations), [uniéndose al grupo BETA](#app-versions), y [mucho más](CONTRIBUTING.md#i-would-like-to-contribute-but-im-not-a-developer). ¡Todo el mundo es bienvenido!

## Licencia

Copyright 2020 CITIUS - USC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
