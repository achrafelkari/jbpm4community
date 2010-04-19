echo To run this, make sure that at least you have jbpm.parent.dir specified in your ${user.home}/.jbpm4/build.properties
cmd /C mvn -U -Pdistro clean install
cmd /C ant -f qa/build.xml reinstall.jbpm
cmd /C ant -f modules/distro/src/main/files/install/build.xml demo.setup.jboss

echo 