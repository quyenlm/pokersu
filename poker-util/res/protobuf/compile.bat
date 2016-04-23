set home=..\..\..\
set common_path=%home%\net-api-common\src\main\java\cn\nextop\hobbit\api\common\proto\
set admin_path=%home%\poker-common\src\main\java\com\mrmq\poker\common\proto\
set google_path=..\

set java_admin_out=%home%\poker-common\src\main\java
set java_commont_out=%home%\net-api-common\src\main\java

protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_admin_out%   %admin_path%\game_model.proto
protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_admin_out%   %admin_path%\game_service.proto
rem protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_admin_out%   %admin_path%\rpc.proto
rem protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_admin_out%   %admin_path%\heartbeat.proto

protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_commont_out%   %admin_path%\rpc.proto
protoc.exe --proto_path=%admin_path%  --proto_path=%common_path% --proto_path=%google_path% --java_out=%java_commont_out%   %admin_path%\heartbeat.proto

@pause