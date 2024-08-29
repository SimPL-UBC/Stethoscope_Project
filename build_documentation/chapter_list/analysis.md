# analysis.m
Straightforward script, it generates waveform, spectogram and CWT (Continuous Wavelet Transform).
You don't need to do CWT actually, since spectogram represent almost the same thing with CWT (their different, but 
you can get the same information from spectogram)
The script here is SimPL, as most function is just one line code that certain matlab toolbox have.

## Load File
```matlab
clear; clc;
filename = "69_HB.wav";
[data, fs] = audioread(filename);
```

## Plot Waveform
```matlab
close all;
leftChannel = data(:, 1);
rightChannel = data(:, 2);
t_wave = (0:length(leftChannel)-1) / fs;
figure;
subplot(211);
plot(t_wave, leftChannel);
title('Audio Waveform');
xlabel('Time (s)');
ylabel('Amplitude');
grid on;
```
## Spectogram
```matlab
window = 256;
noverlap = 128;
nfft = 1024;
[s, f, t, p] = spectrogram(leftChannel, window, noverlap, nfft, fs, 'yaxis');
subplot(212);
surf(t, f, 10*log10(p), 'edgecolor', 'none');
axis tight;
view(0, 90);
colormap(jet);
colorbar;
title('Spectrogram');
xlabel('Time (s)');
ylabel('Frequency (Hz)');
ylim([0 5000]);
```
## CWT
```matlab
down_coeff = 10;
cwt_audio = downsample(leftChannel, down_coeff);
cwt_fs = fs/down_coeff;
t_cwt = downsample(t_wave, down_coeff);
figure(2);
cwt(cwt_audio, 'amor', cwt_fs);
caxis([0,0.1]);
```
## Full Code
```matlab
% Load the WAV file
% clear; close all; clc;
% [file, path] = uigetfile('*.wav', 'Select a WAV file');
% filename = fullfile(path, file);
clear; clc
filename = "69_HB.wav";
[data, fs] = audioread(filename);
%% Waveform
close all;
leftChannel = data(:, 1);
rightChannel = data(:, 2);

% Time vector for plotting the waveform
t_wave = (0:length(leftChannel)-1) / fs;

% Plot the left channel waveform
figure;
subplot(211); % Create a subplot for the left channel waveform
plot(t_wave, leftChannel);
title('Audio Waveform');
xlabel('Time (s)');
ylabel('Amplitude');
grid on;

%% Spectogram
% Parameters for the spectrogram
window = 256; % Window length in samples
noverlap = 128; % Number of overlapping samples
nfft = 1024; % Number of FFT points

% Compute the spectrogram

[s, f, t, p] = spectrogram(leftChannel, window, noverlap, nfft, fs, 'yaxis');

% Plot the spectrogram
subplot(212)
surf(t, f, 10*log10(p), 'edgecolor', 'none'); % Convert power to decibels
axis tight;
view(0, 90);
colormap(jet);
colorbar;
title('Spectrogram');
xlabel('Time (s)');
ylabel('Frequency (Hz)');
ylim([0 5000])

%% CWT analysis
down_coeff = 10;
cwt_audio = downsample(leftChannel, down_coeff);
cwt_fs = fs/down_coeff;
t_cwt = downsample(t_wave, down_coeff);
figure(2);
cwt(cwt_audio, 'amor', cwt_fs)
caxis([0,0.1])

%%
figure;
surf(t, f, 10*log10(p), 'edgecolor', 'none'); % Convert power to decibels
axis tight;
view(0, 90);
colormap(jet);
colorbar;
title('Spectrogram');
xlabel('Time (s)');
ylabel('Frequency (Hz)');
ylim([0 1000])
```
