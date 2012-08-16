<?php
// this is a reverse lookup download assistant script - given an image UUID,
// look up the relavent product type, name and station information.
// Sets attachment headers to prompt browser to show save as dialog with user
// friendly file name
require '../inc/db.php';
require '../inc/product-name.php';

// this is the name of the image requested
$img = $_GET['img'];

// conditional GET - if browser sends if modified since, just return since
// client already has image
// Since products are UUID'd no product file will ever have the same name, and,
// as a consequence, will never change!
if (isset ($_SERVER['HTTP_IF_MODIFIED_SINCE'])) {
    header('HTTP/1.0 304 Not Modified');
    exit;
}

// compute the product name (see product-name.php)
$product_name = getProductName($conn,$img);

// get the appropriate mime type
$mime_types = array(
            'png' => 'image/png',
            'jpeg' => 'image/jpeg',
            'jpg' => 'image/jpeg',
            'gif' => 'image/gif',
            'tif' => 'image/tiff',
            'svg' => 'image/svg+xml',
            'svgz' => 'image/svg+xml',
            );


// check if path exists and if not, returned non-scaled image
// this is useful during testing to avoid having to scale thousands of images
$fullpath = $product_root.'/'.$img;
if (!file_exists($fullpath)) {
    $fullpath = $product_root.'/'.str_replace('_scaled', '', str_replace('gif', 'png', $img));
}

// set headers to indicate when modified
header('Last-Modified: ' . gmdate('m/j/y h:i',filemtime($fullpath)));
header('Content-Type: ' . $mime_types[$product_name["ext"]]);
header('Expires: ' . gmdate('D, d M Y H:i:s \G\M\T', time() + 31536000 ) );
header('Content-Disposition: attachment; filename="'.$product_name['fname'].'"');

// flush headers to allow client chance to abort request
ob_clean();
flush();

// finally, write the file out
readfile($fullpath);
?>
