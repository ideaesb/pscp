<?php
require 'inc/product-name.php';
$about_product = 'about/'.$_GET['type'].'.html';
if (! file_exists($about_product)) {
    echo 'No Such Product Type';
    header("HTTP/1.0 404 Not Found");
    return;
}
$img = $_GET['id'];
$product_page = '/pscp-api/productpage/' . $img;
?>
<div id="contentArea">
    <div class="content">
        <table>
            <tr>
                <td id="stationDetail">
                    &nbsp;
                </td>
                <td>
                    <img src="/images/noaa.gif"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">
                    <img width="622" alt="<?php print $img ?>" src="<?php print 'images/product.php?img='.$img ?>"/>
                </td>
            </tr>
            <tr>
                 <td colspan="2">
                    <?php include $about_product ?>
                 </td>
            </tr>
        </table>
    </div>
</div>
<script type="text/javascript">
    pageload = function() {
        $.get("<?php print $product_page ?>", function(data) {
           $("#stationDetail").html(data);
        });
    };
</script>