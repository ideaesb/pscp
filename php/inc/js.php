<?php
/*
 *  This script does the following:
 *  1. bootstrap some javascript references using locations defined in global.php
 *  2. load commonly used libraries
 *  3. allows pages to define javascript but ensure they are all output at page bottom
 *  4. call a pageload function using jquery
 */
?>

<script type="text/javascript">
    pscpHome = '<?php echo $web_root;?>';
    pscpAPI = '<?php echo $api_root;?>';
</script>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
<script type="text/javascript" src="<?php echo $web_root;?>/js/jquery.autocomplete.min.js"></script>
<?php
$count = count($jslist);
for ($i = 0; $i < $count; $i++) {
    echo '<script type="text/javascript" src="';
    echo $jslist[$i];
    echo '"></script>';
}
?>
<script type="text/javascript">
    $(function() {
        if (window.pageload) {
            window.pageload();
        }
    });
</script>