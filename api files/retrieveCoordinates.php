<?php  
include "conn.php";
$Bno=$_POST['Bno'];
$Destination=$_POST['Destination'];
$i="not";


 $sql = "SELECT latitude,longitude FROM location where Bno='$Bno' AND Destination='$Destination' " ;

 $exe=mysqli_query($conn,$sql);

 
	$users = array();
   while($row=$exe->fetch_assoc()){ 
	$row["latitude"];
	  $row["longitude"];

 array_push($users, $row);
}
if($users[0]!=""){
echo json_encode($users);
}else{
	echo $i;
}
	
?>
