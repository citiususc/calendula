![](assets/calendula_promo_google_play.png)
# Calendula

Calendula é un asistente en Android para a xestión de medicación persoal. Está dirixido a aqueles que teñen problemas en seguir o seu réxime de medicación, olvídanse de tomar as súas pílulas ou teñen horarios difíciles de recordar.

A aplicación está dispoñible para a súa descarga en Google Play, F-Droid e Github. 

<table>
    <tr>
        <td align="center"><a href="https://play.google.com/store/apps/details?id=es.usc.citius.servando.calendula"><img src="https://play.google.com/intl/en_us/badges/images/badge_new.png" alt="Get it on Google Play" ></a></td>
        <td align="center"><a href="https://f-droid.org/packages/es.usc.citius.servando.calendula/"><img src="https://gitlab.com/fdroid/artwork/raw/master/badge/get-it-on.png" alt="Get it on F-Droid" height="68"></a></td>
        <td align="center"><a href="https://github.com/citiususc/calendula/releases/latest"><img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" alt="Get it on Github" height="68"></a></td>
    </tr>
</table>

Visite a nosa páxina web para máis información [https://citius.usc.es/calendula/](https://citius.usc.es/calendula/)


## Novas de Calendula

Temos boas novas!
 
O Servizo Galego de Saúde (SERGAS) adoptou Calendula para conectarse co seu Sistema de Receita Electrónica. Isto levou a varias extensións da versión libre, incluíndo: 
 
 * Descarga automática da pauta de medicación do paciente. 
 * Acceso automático ao calendario de recollida de medicamentos.
 * Recomendación automática das mellores datas de recollida para optimizar as visitas á farmacia. 
 * Acceso ao réxime de dose de anticoagulantes. 
 
O proceso de integración centrouse en dous aspectos fundamentais: a interoperabilidade e a seguridade. 
 
No que se refire a a interoperabilidade, a adopción do estándar internacional HL7-FHIR facilitará a integración de Calendula con outros sistemas de saúde. 
 
En canto á seguridade, adoptouse a especificación OpenID Connect, que permite a Calendula verificar a identidade dos usuarios a partir dunha autenticación realizada polos servidores do SERGAS. 
 
Ao longo deste proceso, traballamos en moitas melloras, incluíndo: 
 * Aumentar o nivel de desenvolvemento da API de Android a 29.
 * Adaptar as notificacións e os servizos en segundo plano aos requirimentos das novas versións de Android. 
 * Actualizar as versións de Gradle, Java e Kotlin. 
 * Migrar as bibliotecas de Android a AndroidX
 * Actualizar as versións da bibliotecas: Google, Iconics, Material-Drawer, Fast Adaptador, ButterLnife, Caldroid e Android Jobs. 
 * Melloras da interface de usuario para solucionar fallos de actualización e NPE. 
 * Actualización de internacionalización: engade novos idiomas e actualiza os que actualmente son compatibles.
 * Melloras de rendemento no uso da memoria: migrar recursos gráficos tipo mapa de bits ao formato vectorial. 
 * Notificacións e aforro de batería: engadiuse a opción de excluír a aplicación do sistema de aforro de batería para evitar que o sistema suspenda a aplicación e impida a entrega de notificacións ao usuario. 
 * Corrección de erros menores. 
 
Estes cambios estarán dispoñibles en breve neste repositorio e aplicaranse á versión de Google Play!

## Comezando

Estas instrucións permitiranlle obter unha copia do proxecto listo para usar na súa máquina local. Se quere contribuír ó desenvolvemento da aplicación, por favor diríxase á sección Contribuír. 

### Preparación do contorno de desenvolvemento

Para o desenvolvemento da aplicación, empregamos [Android Studio](https://developer.android.com/studio/index.html) (o IDE oficial de Android), así que lle recomendamos que o empregue no seu contorno de desenvolvemento. Unha vez instalado Android Studio, pode empregar o Android SDK Manager para obter as ferramentas, plataformas e outras compoñentes que necesitará para comezar o desenvolvemento. As máis importantes son:

* Android SDK Tools e Android SDK Platform-tools (soe ser boa idea actualizar á última versión).
* Android SDK Build-Tools 27.0.3.
* Android 8.1 (API Level 27) SDK Platform.
* Android Support Repository

Tamén pode instalar outros paquetes como emuladores para executar a aplicación, se non dispón ou non quere empregar un dispositivo hardware. A mínima versión de Android soportada é *4.1, Jelly Bean (API level 16).*

### Compilando e instalando a aplicación

Antes de comezar necesita o código fonte, así que proceda a clonar este repositorio na súa máquina local:

```bash
git clone https://github.com/citiususc/calendula.git
cd calendula
```

Android Studio emprega Gradle como a compoñente principal do mecanismo de compilación, pero non é necesario instalalo por separado. En vez diso, pode empregar o xa inclúido [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). Para compilar a aplicación, abra unha terminal na carpeta do repositorio e execute:

```bash
./gradlew clean assembleDevelopDebug
```
*Nota: "developDebug" é a [build variant](https://developer.android.com/studio/build/build-variants.html) que empregamos para desenvolvmento. Para outras variantes, por favor revise o arquivo `Calendula/build.gradle`.*

Despois, poderá instalar a aplicación nun dispositivo ou emulador: 

```bash
adb install Calendula/build/apk/develop/debug/developDebug-[version].apk
```
Estas tarefas tamén se poden executar dende Android Studio con algúns clicks. 

## Versións da aplicación

Neste momento mantemos lanzamentos de Calendula en Google Play, F-Droid y Github.

 * A última versión da app dispoñible nesas páxinas reflexa o código da rama `master`.
 * As ramas de lanzamento distribúense normalmente a través do *Google Play BETA channel* antes de facelas públicas. Se quere ser membro da comunidade de probas, únase ó grupo de probas en Google Groups e automáticamente recibirá as actualizacións da canle BETA como cualquera outra actualización de Google Play. 
 
> Unirse á canle BETA: [faga click aquí!](https://play.google.com/apps/testing/es.usc.citius.servando.calendula)

Revise as [normas de contribución](CONTRIBUTING.md) para máis información acerca do modelo de ramas.

## Qué pinta ten?

Intentamos seguir os principios de [Material Design](https://material.google.com/#). Que pensa do resultado?

  | <img src="assets/screenshots/v2.5-en/1.png" width="230px"/>  | <img src="assets/screenshots/v2.5-en/2.png" width="230px"/> | <img src="assets/screenshots/v2.5-en/8.png" width="230px"/>
  |:---:|:---:|:---:|
  | <img src="assets/screenshots/v2.5-en/3.png" width="230px"/> | <img src="assets/screenshots/v2.5-en/5.png" width="230px"/> | <img src="assets/screenshots/v2.5-en/4.png" width="230px"/>


## Traballo futuro

Temos moitas ideas para novos desenvolvementos e tamén estamos abertos a outras novas. As seguintes novas características poderían ser útiles:

* Información de farmacias cercanas, a súa localización e horarios. 
* Asistente de viaxe: cántas pílulas necesito para este fin de semana?)
* Introducir conceptos de [ludificación](https://en.wikipedia.org/wiki/Gamification) para mellorar a adhesión ó tratamento.

## Atribución do deseño

Neste momento estamos a empregar os seguintes recursos:

* [People Vector Pack](http://www.freepik.com/free-vector/people-avatars_761436.htm) por [Freepik](http://www.freepik.com)
* Iconos [Baby](http://www.flaticon.com/free-icon/baby_136272), [Dog](http://www.flaticon.com/free-icon/dog_194178) e [cat](http://www.flaticon.com/free-icon/cat_194179) por <a href="https://www.flaticon.com/" title="Flaticon">Flaticon</a> (<a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>)
* [Animación de Espertador](https://dribbble.com/shots/1114887-Alarm-Clock-GIF) por  [Daan De Deckere](http://daandd.be/)

## Contribuír

Pode facer un fork e enviar un pull request se quere contribuir ó proxecto. Calendula segue os termos da licenza da [GNU General Public License (v3)](LICENSE.md), así que ó subir contidos ó repositorio estará liberando o seu traballo segundo esta licenza. 

Antes de comezar, revise as nosas [normas de contribución](CONTRIBUTING.md).

### Quero contribuir, pero non son desenvolvedor...

Se non é un desenvolvedor pero quere axudar, non se preocupe! Pode axudarnos a [traducir a aplicación](CONTRIBUTING.md#help-with-app-translations), [uníndose á canle BETA](#app-versions), e [moito máis](CONTRIBUTING.md#i-would-like-to-contribute-but-im-not-a-developer). Todo o mundo é benvido!

## Licenza

Copyright 2020 CITIUS - USC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
