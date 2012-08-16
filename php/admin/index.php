<?php

set_include_path('.' . PATH_SEPARATOR . '..');
$pscp_context = "admin/";
require('inc/global.php');

if (!isset($_COOKIE["u"]) || strlen($_COOKIE["u"]) == 0) {
    header("303 See Other HTTP/1.1");
    header("Location: $api_root/login?ret=$web_root/admin/" ) ;
    header("Referrer: $web_root/admin/" ) ;
    return;
}
require_once( 'inc/db.php' );
// try to keep from caching
header( "Expires: Mon, 1 Jan 1992 01:00:00 GMT" );
header( "Cache-Control: no-cache, must-revalidate" );
header( "Pragma: no-cache" );

$content_header = 'inc/header.php';
$content_leftnav = 'inc/leftnav.php';
$contentprefix = "admin/content";
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>PSCP Admin</title>
        <link rel="shortcut icon" href="<?php echo $web_root;?>/favicon.ico" />
        <link rel="stylesheet" type="text/css"  href="<?php echo $web_root;?>/css/style.css" />
        <link rel="stylesheet" type="text/css" href="<?php echo $web_root;?>/css/weather.css" />
        <link rel="stylesheet" type="text/css" href="<?php echo $web_root;?>/css/admin.css" />
        <link rel="stylesheet" type="text/css" href="<?php echo $web_root;?>/css/jquery.autocomplete.css"></link>
    </head>
    <body>
        <?php require ('inc/container.php'); ?>
        <script type="text/javascript">
            pscpHome = '<?php echo $web_root;?>';
            pscpAPI = '<?php echo $api_root;?>';
        </script>
        <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="<?php echo $web_root;?>/js/jquery.autocomplete.min.js"></script>
        <script type="text/javascript" src="<?php echo $web_root;?>/js/jquery.history.js"></script>
        <script type="text/javascript" src="<?php echo $web_root;?>/js/admin.js"></script>
        <script type="text/javascript" src="<?php echo $web_root;?>/js/admin-forms.js"></script>
        <iframe style="display:none" id="history"></iframe>
        <div id="helpViewer">
            <h4>Click to Close</h4>
            <div></div>
        </div>
    </body>
</html>
