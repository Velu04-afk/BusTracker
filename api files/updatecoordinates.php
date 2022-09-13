<?php
header('Access-Control-Allow-Origin: *');
include "conn.php";
$Bno=$_POST['Bno'];
$Name=$_POST['Name'];
$latitude=$_POST['latitude'];
$longitude=$_POST['longitude'];


$query="UPDATE location SET latitude='$latitude', longitude='$longitude' WHERE Bno='$Bno' AND Name='$Name'";

$res=mysqli_query($conn,$query);
if ($res) {
	echo "true";
}else{
	echo "false";
}
  ?>


