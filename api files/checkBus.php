<?php
include "conn.php";
$Bno=$_POST['Busno'];
$i="not";

$query="SELECT Source,Destination FROM BusCheck where Busno='$Bno'";
$exe=mysqli_query($conn,$query);
//print_r($exe);
if ($exe->num_rows>0) {
	# code...
	$products = array();
while($row=$exe->fetch_assoc()){
	 $row["Source"];
	 $row["Destination"];
	
 array_push($products, $row);
}
echo json_encode($products);
}else{
	echo $i;
}
?>