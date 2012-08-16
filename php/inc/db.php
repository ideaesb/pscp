<?php

require 'global.php';
$conn = pg_connect("host=$pscp_db_host port=5432 dbname=$pscp_db_name user=$pscp_db_user password=$pscp_db_pass");


// processes
$sw = 0;
$hr = 0;
$hs = 0;

$station_count = 0;

if (!$conn) {
    echo "Could not connect to database...";
    exit;
}
else {
    $myresult = pg_exec($conn, "SELECT COUNT(*) FROM stations WHERE process='SW'");
    $sw = pg_result($myresult, 0, 0);

    $myresult = pg_exec($conn, "SELECT COUNT(*) FROM stations WHERE process='HR'");
    $hr = pg_result($myresult, 0, 0);

    $myresult = pg_exec($conn, "SELECT COUNT(*) FROM stations WHERE process='HS'");
    $hs = pg_result($myresult, 0, 0);

    $station_count = $sw + $hr + $hs;

}

/* This will load the currently logged in user's name via the value of the
 * "u" cookie which if set is the user's UUID
 */
$currentUser = "";
if (isset($_COOKIE["u"])) {
    // sanitize cookie for security
    $contactid = addslashes($_COOKIE["u"]);
    // if format of cookie value matches UUID format, issue query
    if (strlen($contactid) == 36 && substr_count($contactid,'-') == 4) {
        // At this point, it should be nearly impossible to perform SQL injection
        $myresult = pg_exec($conn, "SELECT person from contacts where contactid='$contactid'");
        $currentUser = pg_result($myresult,0,0);
    }
}


?>
