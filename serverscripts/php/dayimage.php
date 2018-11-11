<?php
// parse the url
function curPageURL() {
 $pageURL = 'http';
 if ($_SERVER["HTTPS"] == "on") {$pageURL .= "s";}
 $pageURL .= "://";
 if ($_SERVER["SERVER_PORT"] != "80") {
  $pageURL .= $_SERVER["SERVER_NAME"].":".$_SERVER["SERVER_PORT"].$_SERVER["REQUEST_URI"];
 } else {
  $pageURL .= $_SERVER["SERVER_NAME"].$_SERVER["REQUEST_URI"];
 }
 return $pageURL;
}

//$url = 'http://gis.osu.edu/hackoht18/php/dayimage.php?camera=1&dateindex="2018-10-27"&timeindex="09:30:00" ';
//print_r(parse_url($url));// print_r(parse_url(curPageURL()));
//echo parse_url($url, PHP_URL_PATH);
//echo curPageURL();
$query = parse_url(curPageURL(), PHP_URL_QUERY);
$db = new PDO('sqlite:/html/hackoht18/historicimages/dayimages.db') or die("cannot open the database");
if (!$db) { echo "Access database failed\n";}
$query = str_replace("&"," and ",$query);
$query = str_replace("%22","\"",$query);
$query = "SELECT image FROM dayimages where " . $query;
//echo $query;
$results = $db->query($query);
if (!$results) { echo "false in query\n";}
$row = $results->fetch(PDO::FETCH_ASSOC);
$image = $row['image'];
header("Content-type: image/jpeg");
echo  $image;
$db = null;
imagedestroy ($image)

?>