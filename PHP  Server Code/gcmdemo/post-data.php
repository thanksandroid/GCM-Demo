<?php

require_once './GCM.php';

if (isset($_POST["message"]) && isset($_POST["regid"])) {
    $message = $_POST["message"];
    $regId = $_POST["regid"];

    $timestamp = date("Y-m-d H:i:s");

    $msg = array('message' => $message, 'timestamp' => $timestamp);
    $ids = array();
    array_push($ids, $regId);
    // You can send message to 10 ids in one request.
    //Push all(max 10) reg ids to $ids array.

    $gcm = new GCM();

    $response = $gcm->send_notification($ids, $msg);
    echo "Notification sent.";
} else {
    echo 'Invalid data';
}
