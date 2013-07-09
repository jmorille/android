
## Define system variable
############################
ANDROID_HOME=


## Define ~/.m2/settings.xml
############################
		<profile>
			<id>release</id>
			<activation>
				<property>
					<name>release</name>
					<value>release</value>
				</property>
			</activation>
			<properties>
				<!-- maven.test.skip>true</maven.test.skip -->
				 <sign.keystore>~/.keystore</sign.keystore>
                 <sign.storepass>PASS</sign.storepass>
                 <sign.keypass>PASS</sign.keypass>
 			</properties>
 		</profile>
 		
 		
## maven android repository
############################

# Pour le creer a partir du ANDROID_HOME
git clone https://github.com/mosabua/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer

mvn install -P 4.0.3

## svg-android
############################

wget http://svg-android.googlecode.com/files/svg-android-1.1.jar
mvn install:install-file -DgroupId=com.larvalabs.svgandroid -DartifactId=svg-android -Dversion=1.1 -Dpackaging=jar -Dfile=svg-android-1.1.jar

wget http://svg-android-2.googlecode.com/files/svgAndroid2.12Dec2011.jar
mvn install:install-file -DgroupId=com.larvalabs.svgandroid -DartifactId=svg-android2 -Dversion=12Dec2011 -Dpackaging=jar -Dfile=svgAndroid2.12Dec2011.jar


## osmdroid
############################
Project : http://code.google.com/p/osmdroid/

## mapsforge
############################
Project : http://code.google.com/p/mapsforge/

wget http://mapsforge.googlecode.com/files/mapsforge-map-0.3.0-jar-with-dependencies.jar
mvn install:install-file -DgroupId=org.mapsforge -DartifactId=mapsforge-map -Dversion=0.3.0 -Dpackaging=jar -Dfile=mapsforge-map-0.3.0-jar-with-dependencies.jar

## TODO List
############################
Push/Subribe : https://github.com/tokudu/AndroidPushNotificationsDemo
Clound Notification : http://developer.android.com/guide/google/gcm/gs.html

## Map Sample project
############################
http://wiki.openstreetmap.org/wiki/Android

 https://code.google.com/p/androzic/source/browse/src/com/androzic/MapActivity.java
 http://code.google.com/p/big-planet-tracks/source/browse/
