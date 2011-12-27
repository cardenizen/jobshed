<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title><g:layoutTitle default="MnRoad" /></title>
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'main.css')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'mnr.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:javascript library="jquery" plugin="jquery"/>
        <g:javascript library="prototype/prototype" />
        <g:javascript library="prototype/effects" />
        <g:javascript src="application.js" type="text/javascript" />
        <g:javascript src="JSONeditor.js" type="text/javascript" />
        <meta name="gmapkey" content="ABQIAAAAPZMJngdDwEVH8HNIYrfX4RT2yXp_ZAY8_ufC3CFXhHIE1NvwkxTDIQlwf2DZ2gVRWAnInOwLUaHDrw" />
        <g:layoutHead></g:layoutHead>
    </head>
    <body>
        <div style="display: table; #position: relative; overflow: hidden;">
        <div style=" height:50px;">
            <img src="${resource(dir:'images',file:'mnroad.jpg')}" alt="MnRoad"
               STYLE="position:relative; overflow: scroll; WIDTH: 230px; HEIGHT: 50px; LEFT: 0px; TOP: 0px;" />
            </div>
            <div  class="appHeader" style="  HEIGHT: 50px; #position: absolute; LEFT: 230px; TOP: 0px; display: table-cell; vertical-align: middle;">
              MnDOT Road Research DB Job Control<br>
            <span class="menuButton"><mr:signOutLink/></span>
            </div>
        </div>
        <div id="spinner" class="spinner" style="display:none;">
          <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>
        <g:layoutBody />
        <hr>
        <div class='footer'>
          <p>
            <g:if test="${grailsApplication?.metadata['app.version']}">
              v${grailsApplication.metadata['app.version']}
              (Build #${grailsApplication.metadata['app.buildNumber']}
                  ${grailsApplication.metadata['app.buildDate']}
               &nbsp;
                  ${grailsApplication.metadata['app.buildProfile']})
            </g:if>
            Brought to you by <a href="http://www.dot.state.mn.us/">Minnesota Department of Transportation</a>, powered by <a href="http://grails.org/">Grails</a>
            <g:if test="${grailsApplication?.metadata['app.grails.version']}">
              &nbsp;${grailsApplication?.metadata['app.grails.version']}
            </g:if>
            MnROAD Cell Maps (June 2010)&nbsp;<a href="http://mrl2k3loader.ad.dot.state.mn.us/CellMapsFiles/MnROAD Cell Maps ML (June 2010).pdf">ML</a>
            &nbsp;<a href="http://mrl2k3loader.ad.dot.state.mn.us/CellMapsFiles/MnROAD Cell Maps LVR (June 2010).pdf">LVR</a>
          </p>
        </div>
    </body>
</html>