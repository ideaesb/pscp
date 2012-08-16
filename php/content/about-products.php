<div id="contentArea">
  <h1>About Pacific Storms Climatology Products</h1>
  <div class="content">
    <?php include 'about/1.1.html'; ?>
    <?php include 'about/2.1.html'; ?>
    <?php include 'about/3.1.1.html'; ?>
    <?php include 'about/4.1.html'; ?>
    <?php include 'about/5.1.1.html'; ?>
    <?php include 'about/5.1.2.html'; ?>
    <?php include 'about/5.2.1.html'; ?>
    <?php include 'about/6.1.html'; ?>
    <?php include 'about/7.1.html'; ?>
    <?php include 'about/7.2.html'; ?>
    <?php include 'about/8.1.html'; ?>
    <?php include 'about/9.1.html'; ?>
  </div>
</div>
<script type="text/javascript">
    function show(id) {
        var next = $("#" + id).show().next();
        while (! /^h./i.exec(next.get(0).nodeName)) {
            next.show();
            next = next.next();
        }
    }
    function hidedetails() {
        $("#contentArea p").hide();
        $("#contentArea ul").hide();
    }
    function pageload() {
        if (window.location.hash.length > 1) {
            hidedetails();
            $("#contentArea h2").hide();
            show(window.location.hash.substr(1).replace(/\./g,'-'));
        } 
    }
</script>