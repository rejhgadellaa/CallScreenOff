
<!--

	ICERRR

	REJH Gadellaa 2015

-->

<!doctype html>

<html lang="en">

<head>

	<!-- UTF-8 -->
    <meta charset="utf-8">

    <!-- Title + Info -->
    <title>CallScreenOff</title>

	<!-- Meta -->
	<meta content="True" name="HandheldFriendly" />
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport" />

    <meta name="title" content="CallScreenOff" />
    <meta name="description" content="Keep your screen locked">
	<meta name="author" content="REJH Gadellaa">

    <!-- OpenGraph Snippet for fb, g+ -->
    <meta property="og:site_name" content="CallScreenOff" />
    <meta property="og:title" content="CallScreenOff" />
    <meta property="og:type" content="article" />
    <meta property="og:image" content="http://rejh.nl/callscreenoff/img/ic_teal_512_spaced.png" />
    <meta property="og:description" content="The app that keeps your screen turned off for Android" />

    <!-- IE: Use Chrome Frame if available, else latest IE9 renderer -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">

	<!-- Icon -->
    <link rel="icon" href="img/ic_launcher.png" type="image/x-icon">

	<!-- Javascript: jQuery -->
    <script language="javascript" type="text/javascript" src="js/jquery/jquery-1.11.3.min.js?c=<?=time();?>"></script>

    <!-- Javascript: Things :D -->
    <script>
	if (window.location.hostname.indexOf("www.rejh.nl")<0 && window.location.hostname.indexOf("localhost")<0 && window.location.hostname.indexOf("192.168.")<0) {
		window.location.href = "https://www.rejh.nl/callscreenoff/"; // for prod
	}
	</script>
	</script>

    <!-- Style / base -->
    <link rel="stylesheet" type="text/css" href="css/common.css?c=<?=filemtime("css/common.css");?>" />
    <link rel="stylesheet" type="text/css" href="css/default.css?c=<?=filemtime("css/default.css");?>" />

    <!-- Style / responsive -->
	<link rel="stylesheet" type="text/css" href="css/size.phone.css?c=<?=filemtime("css/size.phone.css");?>" media="screen and (max-width: 960px)" />

    <!-- Webfonts -->
    <link href='http://fonts.googleapis.com/css?family=Roboto:100,200,300,400' rel='stylesheet' type='text/css'>

    <!--[if lt IE 9]>
    	<script src="js/plugs/html5shiv.js"></script>
    <![endif]-->

    <script>

        var vars = {};

        function onload() {

            console.log("onload()");

            // Scroll listener -> Hide fab :D
            $(window).off( 'scroll');
            $(window).on( 'scroll', function(e) {

                if ($(window).scrollTop()<136) {
                    $(".header_sml .logo_sml").css("opacity",0);
                    $(".header").css("opacity",1);
                }
                if ($(window).scrollTop()>136) {
                    $(".header_sml .logo_sml").css("opacity",1);
                    $(".header").css("opacity",0);
                }

            });

            // Load readme..
            $( "#readme" ).load( "https://raw.githubusercontent.com/rejhgadellaa/CallScreenOff/master/web/ajax/readme.md?c="+new Date().getTime(), function() {
              console.log( " -> Loaded readme" );
              $(".footer").css("display","block");
            });
        }

    </script>

</head>

<body onload="onload();">

<!-- Section: home -->
<section id="home">

    <!-- Header -->
	<div class="header">

        <div class="logo">
            <img src="img/ic_white_512_spaced.png" />
            <div class="title">CallScreenOff</div>
            <div class="subtitle"><span class="theapp">The app that keeps your screen turned off -</span> <span class="forandroid">for Android</span></div>
        </div>

    </div>

    <!-- Header_sml -->
    <div class="header_sml shadow_z2">

        <div class="logo_sml">
            <img src="img/ic_launcher_w_48.png?c=<?=time();?>" />
            <div class="title">CallScreenOff</div>
            <div class="subtitle">The (clock radio) icecast streaming app</div>
        </div>

    </div>

    <!-- Main -->
    <div class="main">
        <div class="main_inner">

            <!-- readme -->
            <div id="readme" class="readme"><p>Loading...</p></div>

            <!-- footer -->
            <div class="footer">

                <!-- Ruler -->
                <div class="ruler"></div>

                <!-- Legal -->
                <div class="legal">
                    <div class="item_wrap">CallScreenOff is open source and distributed under the MIT license</div>
                    <div class="item_wrap">Copyright &copy; <?=date("Y");?> <a href="http://www.rejh.nl/" target="_blank">REJH Gadellaa</a></div>

                </div>

                <!-- Social -->
                <div class="social">
                    <div class="item_wrap"><div class="g-plusone" data-href="http://www.rejh.nl/callscreenoff/" data-size="medium" data-annotation="inline" data-width="200"></div></div>
                    <div class="item_wrap"><a href="https://twitter.com/share" class="twitter-share-button" data-via="RGadellaa" data-count="none">Tweet</a></div>
                </div>

            </div>

        </div>
    </div>

    <!-- Guideline -->
    <div class="guideline one"></div>
    <div class="guideline two"></div>

</section>

<script>
// Google+
	(function() {
		var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
		po.src = 'https://apis.google.com/js/plusone.js';
		var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
	})();
	// Twitter
	!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");
</script>

</html>
