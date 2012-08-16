<?php
// Templating index page

// include global variables first
require('inc/global.php');

// define header, nav names
$content_header = 'header.php';
$content_leftnav = 'leftnav.php';

// define directory for include resolution
// see inc/container.php
$contentprefix = 'content';

// page title is defined in included content page
// but this allows for a default
$page_title = '';

// Begin output buffering
// Variables declared in included pages will be available later
// but output will be delayed until we are ready.
// This has the added benefit of speeding up Apache (anecdotal)
ob_start();

// do content templating
require ('inc/container.php');
require ('inc/footer.php' );

// now render all javascript (it's supposed to go at the bottom of the page)
require_once ('inc/js.php' );

// compute page title
$end_page_title = '';
if (strlen($page_title) > 0) {
    $end_page_title = ' - '.$page_title;
}

// finally, get all output and clean the buffer
$body_content = ob_get_clean();
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Pacific Storms Climatology Products (PSCP)<?php echo $end_page_title?></title>
        <link rel="shortcut icon" href="<?php echo $web_root;?>/favicon.ico" />
        <link rel="stylesheet" type="text/css"  href="<?php echo $web_root;?>css/style.css" />
        <link rel="stylesheet" type="text/css" href="<?php echo $web_root;?>css/weather.css" />

       	<?php
       		if (isset($csslist)) {
       		   $count = count($csslist);
	           for ($i = 0; $i < $count; $i++) {
	               echo '<link rel="stylesheet" type="text/css" href="/';
	               echo $csslist[$i];
	               echo '"/>';
	           }
	        }
        ?>
    </head>
    <body>
       
        <?php echo $body_content ?>
        <script type="text/javascript">
            $(function() {
                if (window.location.search.indexOf('ni') > 0) {
                    $(".ni").css({
                        "text-decoration" : "blink",
                        color : "red"
                    });
                }
                $(".ni").click(function() { alert("Not Implemented"); return false;});
            });
        </script>
    </body>
</html>
