package com.example.tcpapplication;

public record DeviceMessage(String deviceId, String metric, String value) {}
