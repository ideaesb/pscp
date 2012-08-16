<?php
require_once 'constants.php';
if (!isset($pscp_context)) {$pscp_context = '';}
$web_root= str_replace($pscp_context.'index.php', '', $_SERVER['PHP_SELF']);
if ($web_root[strlen($web_root) - 1] == '/') {
    $web_root = substr($web_root, 0, $web_root - 1);
}
?>