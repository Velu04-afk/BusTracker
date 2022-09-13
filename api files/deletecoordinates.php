<?php
header('Access-Control-Allow-Origin: *');
include "conn.php";
$Bno=$_POST['Bno'];
$Name=$_POST['Name'];
$Destination=$_POST['Destination'];



$query="DELETE FROM location WHERE  Bno=$Bno AND Name='$Name' AND Destination='$Destination' ";
$res=mysqli_query($conn,$query);

if ($res) {
	echo "true";
}else{
	echo "false";
}
  ?>