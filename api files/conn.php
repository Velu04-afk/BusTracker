<?php 
$servername='localhost';
$username='id12307345_akshay';
$password='akshay@123';
$db='id12307345_location';

$conn=new mysqli($servername,$username,$password,$db);
if ($conn->connect_error) {
	die("connection failed".$conn->connect_error);
	# code...
}else{
//	echo "connected successfully";
}
?>