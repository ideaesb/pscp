<?php
require 'db.php';

function getProductName($conn,$img) {
    // strip off any _scaled suffix and ensure image is sanitized for SQL statement
    if ( strripos($img, '_scaled') >= 0) {
        $imgsql = addslashes(urldecode(str_replace('_scaled', '', $img)));
    } else {
        $imgsql = addslashes(urldecode($img));
    }

    // lookup the image
    $imgsql = array_shift(explode('.',$imgsql)) . '%';
    $rs = pg_exec($conn, "select * from productrevlocation
    WHERE location LIKE '$imgsql'");

    if (pg_numrows($rs) == 0) {
        error_log("Unable to find product with location:". $imgsql);
        header("HTTP/1.0 404 Not Found");
        exit;
    }
    // combine the parts of the product type, name, etc
    // to make a nice name for the image
    $parts = array(
        pg_result($rs,0,0),
        pg_result($rs,0,1),
        pg_result($rs,0,2),
        pg_result($rs,0,3),
        pg_result($rs,0,4),
        pg_result($rs,0,5),
        pg_result($rs,0,6),
        pg_result($rs,0,7),
        pg_result($rs,0,8),
        pg_result($rs,0,9)
    );
    $original = pg_result($rs,0,10);
    $ext = strtolower(array_pop(explode('.',$img)));
    $pname = implode(" ", $parts);
    $pname = preg_replace('/\s\s+/', ' ', $pname);
    $fname = $pname . ".$ext";
    return array("fname" => $fname, "ext" => $ext, "name" => $pname);
}
?>