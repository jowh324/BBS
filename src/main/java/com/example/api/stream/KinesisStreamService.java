package com.example.api.stream;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesisvideo.KinesisVideoClient;
import software.amazon.awssdk.services.kinesisvideo.model.APIName;
import software.amazon.awssdk.services.kinesisvideo.model.GetDataEndpointRequest;
import software.amazon.awssdk.services.kinesisvideo.model.GetDataEndpointResponse;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.KinesisVideoArchivedMediaClient;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.model.GetHlsStreamingSessionUrlRequest;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.model.GetHlsStreamingSessionUrlResponse;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.model.HLSPlaybackMode;

@Service
public class KinesisStreamService {

    private final Region region;
    private final String streamName;

    public KinesisStreamService(
            @Value("${aws.region}") String region,
            @Value("${kinesis.stream-name}") String streamName
    ) {
        this.region = Region.of(region);
        this.streamName = streamName;
    }

    public String issueLiveHlsUrl() {
        try (KinesisVideoClient videoClient = KinesisVideoClient.builder()
                .region(region)
                .build()) {

            GetDataEndpointResponse endpointResponse = videoClient.getDataEndpoint(
                    GetDataEndpointRequest.builder()
                            .streamName(streamName)
                            .apiName(APIName.GET_HLS_STREAMING_SESSION_URL)
                            .build()
            );

            String endpoint = endpointResponse.dataEndpoint();

            try (KinesisVideoArchivedMediaClient archivedMediaClient =
                         KinesisVideoArchivedMediaClient.builder()
                                 .region(region)
                                 .endpointOverride(URI.create(endpoint))
                                 .build()) {

                GetHlsStreamingSessionUrlResponse hlsResponse =
                        archivedMediaClient.getHLSStreamingSessionURL(
                                GetHlsStreamingSessionUrlRequest.builder()
                                        .streamName(streamName)
                                        .playbackMode(HLSPlaybackMode.LIVE)
                                        .expires(300)
                                        .build()
                        );

                return hlsResponse.hlsStreamingSessionURL();
            }
        }
    }
}