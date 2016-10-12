# web-app
Web conversion to Android applications



# web-app
Web conversion to Android applications

We recommend the use of Andriod Studio

1.
Open \workspace\myApp\res 

Replace one of the two pictures to your own(include drawable-xxxx), to use a transparent PNG format, otherwise there will be white edge around

Two pictures are welcome interface and LOGO

2.
Open res->values->strings.xml , Replace app_name 

3.
Open res->XML>config.xml
Find <content src="http://your webside "> Replace your Webside

Package files into APP 

Let APP achieve automatic upgrade and update
  
Set the XML file and upload, open the src/com/myapp/myApp.java delete the following two lines of code

//UpdateManager manager = new UpdateManager(myApp.this);
//manager.checkUpdate();



