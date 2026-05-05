# Communication System

## Welcome to our Communication System Project Repo

This repo exists for the purpose of completing our CS 401: Software Engineering - Group Project.

## Overview
Scalable, text-only communications system designed to sustain sending messages between multiple users. These messages can be sent synchronously and asynchronously in group chats or private chats. Users will be limited to messages that are only for them, and all messages will be viewable by IT staff. Users and IT staff interact with the system through the provided GUI.

## Our Communication System
This product is a real-time communication system that runs through a two-way client-server connection. The product allows authenticated users to reach the server and send messages through TCP/IP networking connections. Ultimately, the product should support a large number of concurrent users, with message history stored in a data handling file. The system does not rely on external databases, making it less optimized for retaining large amounts of historical data. 

## How to run

### SERVER:
The server can be run just by running the Server.java source file in eclipse with no arguments
The first run of the server will create 6 test users with no chats each with the username and password of "1" through "6"
Changes will save to the server, so any message sending will update the file structure server side
Changes to the server data files will not update the server while it is running
Server chat files will be saved to "LocalFiles/Server"
Server user files will be saved to "LocalFiles/Users"

### CLIENT:
The client is ran through the Main.java source file in eclipse
The client by default uses "localhost" as the ip, however it does accept an ip as an argument
The ip will only be accepted if it is the only argument
Exported files will be saved to "LocalFiles/IT_Export/Chats"

For specifics on how to use the running program, please refer to the Design Specification, and the Use Cases pdf

## Constraints
1. Application must be developed based on Java
2. The system will not be using databases, libraries, frameworks, or other technologies. 
3. The system follows standard object-oriented design principles
4. This application was developed using Eclipse IDE

## For more information and deeper insights
Please refer to our Software Requirements Specification Document where we have requirements listed, Design Specification, and the Use Cases pdf.

### Thank you for checking out our repo! 
