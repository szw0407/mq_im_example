# MQ IM Example

An Android instant messaging application based on RabbitMQ, supporting both group chat and point-to-point messaging.

## Features

- **Two messaging modes**: IM group chat (Fanout Exchange) and simple queue mode
- **Flexible server configuration**: Custom RabbitMQ server settings
- **Multiple users**: Preset users and custom authentication
- **Real-time messaging**: Live message delivery via RabbitMQ
- **Modern UI**: Clean interface with RecyclerView

## Quick Start

### Requirements
- Android Studio
- RabbitMQ Server

### Setup
1. Start RabbitMQ server
2. Create virtual host `mq_im_example`
3. Create users: user1/00000001, user2/00000002, etc.
4. Open project in Android Studio and run

### Usage
1. Configure server settings (host, port, channel)
2. Select messaging mode (IM group chat or simple queue)
3. Choose user credentials
4. Start sending messages

## Architecture

- `MainActivity.java` - UI and message handling
- `RabbitMqManager.java` - RabbitMQ connection management
- `MsgBean.java` - Message data model
- `MsgAdapter.java` - Message list adapter

## Messaging Modes

**IM Group Chat**: Uses Fanout Exchange - all connected clients receive messages
**Simple Queue**: Uses regular queue - point-to-point message delivery
