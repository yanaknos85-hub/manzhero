package ru.sber.transport.tariff_fleet.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;

@Configuration
@GrpcClientBean(client = @GrpcClient("grpc-files-v2"), clazz = DeleteServiceGrpc.DeleteServiceStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-files-v2"), clazz = DownloadServiceGrpc.DownloadServiceStub.class)
@GrpcClientBean(client = @GrpcClient("grpc-files-v2"), clazz = UploadServiceGrpc.UploadServiceStub.class)
public class FilesGrpcConfig {
}
