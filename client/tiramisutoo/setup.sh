# Installing packages for 'typescript' and avoid errors of the same
echo
echo "***************************************"
echo "1. Installing packages for 'typescript'"
echo "***************************************"
npm install
npm install @ionic/app-scripts@latest --save-dev

# Installing package keyboard to avoid error: Property 'disableScroll' does not exist on type 'Keyboard'.
echo
echo "*******************************"
echo "2. Installing package keyboard "
echo "*******************************"
npm install --save @ionic-native/keyboard@4.12.0
npm audit fix --force

# At this point, this command should work:
# ionic serve

# Installing resources for android
echo
echo "***********************************"
echo "3. Installing resources for android"
echo "***********************************"
ionic cordova resources android

# Installing resources for ios
echo
echo "*******************************"
echo "4. Installing resources for ios"
echo "*******************************"
ionic cordova resources ios

# Installing additional plugins
echo
echo "********************************"
echo "5. Installing additional plugins"
echo "********************************"
# remove old version
#ionic cordova plugin rm cordova-sqlite-storage
#ionic cordova plugin rm cordova-android-support-gradle-release
# install latest version of the package
npm i cordova-sqlite-storage@latest
npm i cordova-android-support-gradle-release@latest
# install latest version of plugin
ionic cordova plugin add cordova-sqlite-storage@latest
ionic cordova plugin add cordova-android-support-gradle-release

# Adding android platform
echo
echo "**************************"
echo "6. Adding android platform"
echo "**************************"
ionic cordova platform add android

# Adding ios platform
echo
echo "**********************"
echo "7. Adding ios platform"
echo "**********************"
ionic cordova platform add ios

# Prepare android platform
echo
echo "**********************"
echo "8. Prepare android.  "
echo "**********************"
ionic cordova prepare android

# Prepare ios platform
echo
echo "**********************"
echo "9. Prepare ios.  "
echo "**********************"
ionic cordova prepare ios

# Copy google-services.json to the right location
echo
echo "***************************************************"
echo "10. Copy google-services.json to the right location"
echo "***************************************************"
cp google-services.json platforms/android/app/
cordova plugin add cordova-support-google-services --save
sed -i '' 's/target=\"google-services/target=\"app\/google-services/g' config.xml

# Modify min sdk version to 19
echo
echo "********************************"
echo "11. Modify min sdk version to 19"
echo "********************************"
sed -i '' 's/cdvMinSdkVersion=.*/cdvMinSdkVersion=19/g' platforms/android/gradle.properties

# Building android platform
echo
echo "**********************"
echo "12. Building android.  "
echo "**********************"
ionic cordova build android

# Building ios platform
echo
echo "**********************"
echo "13. Building ios.  "
echo "**********************"
ionic cordova build ios

# Running in android
echo
echo "******************************"
echo "14. Running the app in android"
echo "******************************"
ionic cordova run android -l

# Running in ios
echo
echo "**************************"
echo "15. Running the app in ios"
echo "**************************"
ionic cordova run ios -l



