# heart_murmur_vis.m
heart_murmur_vis.m contains the code for visualizing heart murmur problem.
We want to know this to show that certain heart problem has a characteristic sound, and
the way we do it is by showing the spectogram, which is a visual representation of 
sound with both temporal and spatial property
time domain HRV feature and the frequency domain HRV feature.

## Load Audio File
```matlab
[normal_sound, normal_fs] = audioread('audio/internal/normal.mp3');
[as_early_sound, as_early_fs] = audioread('audio/internal/as-early.mp3');
[late_early_sound, late_early_fs] = audioread('audio/internal/lateas.mp3');
```
The code here load in audio from a sample heartbeat murmur that you can find more [here](https://depts.washington.edu/physdx/heart/demo.html).

## Spectogram
```matlab
time = 10/1000;
window_length = int32(time*normal_fs);
overlap = round(0.5*window_length);
nfft = 2048;

[s_normal, f_normal, t_normal] = spectrogram(normal_sound, window_length, overlap, nfft, normal_fs);
normal_db = 10*log(abs(s_normal));
figure;
normal_t = 0:1/normal_fs:length(normal_sound)/normal_fs-1/normal_fs;
subplot(211)
plot(normal_t, normal_sound);
title('Normal Sound Waveform')
xlabel('Time (s)')
ylabel('Amplitude')

subplot(212)
pcolor(t_normal, f_normal, normal_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');
```
First 4 line are the parameters. Note the power of 2 for the fft algorithm.

## Full Code
```matlab
clear; clc; close all;
[normal_sound, normal_fs] = audioread('audio/internal/normal.mp3');
[as_early_sound, as_early_fs] = audioread('audio/internal/as-early.mp3');
[late_early_sound, late_early_fs] = audioread('audio/internal/lateas.mp3');

time = 10/1000;
window_length = int32(time*normal_fs);
overlap = round(0.5*window_length);
nfft = 2048;

[s_normal, f_normal, t_normal] = spectrogram(normal_sound, window_length, overlap, nfft, normal_fs);
normal_db = 10*log(abs(s_normal));
figure;
normal_t = 0:1/normal_fs:length(normal_sound)/normal_fs-1/normal_fs;
subplot(211)
plot(normal_t, normal_sound);
title('Normal Sound Waveform')
xlabel('Time (s)')
ylabel('Amplitude')

subplot(212)
pcolor(t_normal, f_normal, normal_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');

[s_as_early, f_as_early, t_as_early] = spectrogram(as_early_sound, window_length, overlap, nfft, as_early_fs);
as_early_db = 10*log(abs(s_as_early));
figure;
as_early_t = 0:1/as_early_fs:length(as_early_sound)/as_early_fs-1/as_early_fs;
subplot(211)
plot(as_early_t, as_early_sound);
title('Aortic stenosis (early) Sound Waveform')
xlabel('Time (s)')
ylabel('Amplitude')

subplot(212)
pcolor(t_as_early, f_as_early, as_early_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');

[s_late_early, f_late_early, t_late_early] = spectrogram(late_early_sound, window_length, overlap, nfft, late_early_fs);
late_early_db = 10*log(abs(s_late_early));
figure;
late_early_t = 0:1/late_early_fs:length(late_early_sound)/late_early_fs-1/late_early_fs;
subplot(211)
plot(late_early_t, late_early_sound);
title('Aortic stenosis (late) Sound Waveform')
xlabel('Time (s)')
ylabel('Amplitude')

subplot(212)
pcolor(t_late_early, f_late_early, late_early_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');

figure;

subplot(311)
pcolor(t_normal, f_normal, normal_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
ylim([0 1000])
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');
title('Normal')

subplot(312)
pcolor(t_as_early, f_as_early, as_early_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
ylim([0 1000])
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');
title('Aortic stenosis (early)')

subplot(313)
pcolor(t_late_early, f_late_early, late_early_db);
shading interp;
xlabel('Time(s)')
ylabel('Frequency(kHz)')
ylim([0 1000])
colorbar
hcb=colorbar;
ylabel(hcb, 'Power/frequency (dB/Hz)');
title('Aortic stenosis (late)')
```
