package com.afandian.langstroth;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioRecorder  {
    private static int RECORDER_SAMPLERATE = 22050; // 44100 crashes
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private String path;


    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    public void startRecording(String path) {
        this.path = path;

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        short buffer[] = new short[BufferElements2Rec];

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(this.path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Header format from
        // http://www.topherlee.com/software/pcm-tut-wavformat.html
        // Annoyingly little-endian
        byte[] header = new byte[] {
                // 0 "RIFF"
                0x52, 0x49, 0x46, 0x46,
                // 4 File size, to fill.
                0x00, 0x00, 0x00, 0x00,
                // 8 "WAVE"
                0x57, 0x41, 0x56, 0x45,
                // 12 "fmt "
                0x66, 0x6D, 0x74, 0x20,
                // 16 length of the above (16)
                0x10, 0x00, 0x00, 0x00,
                // 20 PCM format
                0x01, 0x00,
                // 22 1 channel
                0x01, 0x00,
                // 24 Sample rate, to fill.
                0x00, 0x00, 0x00, 0x00,
                // 28 byte - (Sample Rate * BitsPerSample * Channels) / 8.
                0x00, 0x00, 0x00, 0x00,
                // 32 (BitsPerSample * Channels) / 8.1 - 8 bit mono2 - 8 bit stereo/16 bit mono4 - 16 bit stereo
                0x00, 0x00,
                // 34 Bits per sample, to fill.
                0x00, 0x00,
                // 36 "data"
                0x64, 0x61, 0x74, 0x61,
                // 40 File size, to fill.
                0x00, 0x00, 0x00, 0x00};

        // Sample rate.
        header[24] = (byte)(RECORDER_SAMPLERATE & 0xFF);
        header[25] = (byte)((RECORDER_SAMPLERATE >> 8) & 0xFF);
        header[26] = (byte)((RECORDER_SAMPLERATE >> 16) & 0xFF);
        header[27] = (byte)((RECORDER_SAMPLERATE >> 24) & 0xFF);

        // Byte rate
        int byteRate = RECORDER_SAMPLERATE * BytesPerElement * 1;
        header[28] = (byte)(byteRate & 0xFF);
        header[29] = (byte)((byteRate >> 8) & 0xFF);
        header[30] = (byte)((byteRate >> 16) & 0xFF);
        header[31] = (byte)((byteRate >> 24) & 0xFF);

        // Bits per sample
        header[34] = (byte)((BytesPerElement * 8) & 0xFF);
        header[35] = (byte)(((BytesPerElement * 8) >> 8) & 0xFF);


        try {
            stream.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // blocking read
            recorder.read(buffer, 0, BufferElements2Rec);
            try {
                byte bData[] = short2byte(buffer);
                stream.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            stream.getFD().sync();
            stream.close();

            RandomAccessFile f = new RandomAccessFile(new File(this.path), "rw");

            // Set file size.
            long length = f.length();
            f.seek(40);
            f.write((byte)(length & 0xFF));
            f.write((byte)((length >> 8) & 0xFF));
            f.write((byte)((length >> 16) & 0xFF));
            f.write((byte)((length >> 24) & 0xFF));

            f.getFD().sync();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }
}