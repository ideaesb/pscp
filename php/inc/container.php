<?php

$contentpage = 'home';
if (!empty($_POST['page']))
{
 $contentpage = $_POST['page'];
}
elseif (!empty($_GET['page']))
{
 $contentpage = $_GET['page'];
}

$contentrurl = $contentprefix . "/" . $contentpage . ".php";

$jslist = array();
?>
<div id="container">
 <?php require ( $content_header ); ?>
 <?php require ( $content_leftnav ); ?>
 <?php require ( $contentrurl ); ?>
</div>