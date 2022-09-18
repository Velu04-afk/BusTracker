<?php 
$servername="tcp:divyasingh.database.windows.net,1433";
$username='Divya';
$password='Devesh-2002';
$db='BusTracker';

$conn=new mysqli($servername,$username,$password,$db);
if ($conn->connect_error) {
	die("connection failed".$conn->connect_error);
	# code...
}else{
//	echo "connected successfully";
}
?>
