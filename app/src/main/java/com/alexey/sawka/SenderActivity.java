package com.alexey.sawka;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.alexey.sawka.grpc.VoiceServiceGrpc;
import com.alexey.sawka.grpc.VoiceServiceProto.VoiceRequest;
import com.alexey.sawka.grpc.VoiceServiceProto.VoiceResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class SenderActivity extends AppCompatActivity {

    private static final String TAG = "SenderActivity";
    private ManagedChannel channel;
    private VoiceServiceGrpc.VoiceServiceStub asyncStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        String recognizedText = getIntent().getStringExtra("recognizedText");

        if (recognizedText != null && !recognizedText.isEmpty()) {
            setupGrpcConnection();
            sendTextToServer(recognizedText);
        } else {
            Log.e(TAG, "No recognized text received!");
        }
    }

    private void setupGrpcConnection() {
        channel = ManagedChannelBuilder.forAddress("192.168.1.156", 3000)
                .usePlaintext()
                .build();
        asyncStub = VoiceServiceGrpc.newStub(channel);
    }

    private void sendTextToServer(String recognizedText) {
        StreamObserver<VoiceResponse> responseObserver = new StreamObserver<VoiceResponse>() {
            @Override
            public void onNext(VoiceResponse response) {
                Log.d(TAG, "Ответ от сервера: " + response.getResponse());
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Ошибка при вызове gRPC: ", t);
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "gRPC запрос завершен");
            }
        };

        StreamObserver<VoiceRequest> requestObserver = asyncStub.processVoice(responseObserver);

        try {
            VoiceRequest request = VoiceRequest.newBuilder()
                    .setText(recognizedText)
                    .setSource("smartphone")
                    .build();
            requestObserver.onNext(request);
            requestObserver.onCompleted();
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}