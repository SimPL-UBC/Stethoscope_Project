# HRV.m
HRV.m contains the Heart Rate Variability measurement. These file contains the peak detection algorithm,
time domain HRV feature and the frequency domain HRV feature.

## Preprocessing

### Read File
```matlab
[audio, fs] = audioread('audio/240729_sleep_modified.wav');
downsampling_const = 10;
audio = downsample(audio, downsampling_const);
fs = fs/downsampling_const;
if size(audio, 2) == 2
    audio = mean(audio, 2);
end

audio = audio / max(abs(audio));
```
The code block aboves reads the specified file in audioread, and then perform
these following operation:
- Downsample by *downsampling_const*
- Average out both signal (is not relevant here, since our current microphone records mono)
- Normalize sample to make inspection easier

### Filtering

```matlab
max_time = length(audio)/fs;
time = 0:1/fs:max_time - 1/fs;

cutoffFreq = 60;
segmentTimeWindow = 5;
secondOverlap = 1;

[timeVec, filtered_audio] = butterworthFilter(audio, cutoffFreq, fs, max_time);
filtered_audio = medfilt1(filtered_audio,25);
filtered_audio = filtered_audio / max(abs(filtered_audio));
```
The above block of code does two things, mainly creating the time vector
for plotting and filter the audio.

Filter is done by cutting off certain frequency, specified by *cutoffFreq* and the *butterworthFilter* function, change these variable as suitable.
Median filtering is also done to remove transient peaks

## Peaks detection

Peak detection works in three steps:
- Envelope of the whole function (using hilbert transform)
- Create a local maximum for peak detection amplitude threshold
- Peak detection loop

After that the code calculates the BPM, as now we have all the data we need to calculate BPM

### Envelope
```matlab
% Detect peaks
envelope = abs(hilbert(filtered_audio));
edge_compensation = 10;
envelope = envelope(1:length(envelope)-edge_compensation);
```
These block of code makes an envelope function of your original signal (using hilbert transform).
Notice the *edge_compensation*, this variable is used to account for the edge distortion of hilbert transform, see 
[this excellent explanation](https://dsp.stackexchange.com/questions/76754/how-to-alleviate-the-edging-effect-of-the-hilbert-transform) for further explanation.

```matlab
local_amplitude_window = 10;
peak_window_size = local_amplitude_window * fs; 
peak_step_size = local_amplitude_window * fs; %decided that no overlap is better
all_peaks = [];
all_locs = [];
num_peak_windows = floor((length(envelope) - peak_window_size) / peak_step_size) + 1;

%loop
for j = 1:num_peak_windows
    peak_start_idx = (j-1) * peak_step_size + 1;
    peak_end_idx = peak_start_idx + peak_window_size - 1;
    if peak_end_idx > length(envelope)
        peak_end_idx = length(envelope);
    end
    envelope_window = envelope(peak_start_idx:peak_end_idx);
    local_max = max(envelope_window);
    [window_peaks, window_locs] = findpeaks(envelope_window, MinPeakHeight = local_max * 0.6, MinPeakDistance = fs * 0.5);
    all_peaks = [all_peaks; window_peaks];
    all_locs = [all_locs; window_locs + peak_start_idx - 1];
end
```
The block of code above initialize the vector (although one improvement you can do is to make a big vector with predefine size to speed up calculation)
and create local amplitude for peak detection amplitude threshold. In long recording session, we can't really rely on one max amplitude, which is why these algorithm was implemented.
One thing I wanted to do was to make the local amplitude change, say if it doesn't detect any peaks during for 2 seconds, you know we need to change the local amplitude.
Right now the code uses a set amount of time defined in *local_amplitude_window*. 

The rest of the code from here is a simple bpm calculation and plotting

## HRV Time Domain Calculation
```matlab
s1s1_intervals = diff(all_locs);
diff_s1s1_intervals = diff(s1s1_intervals);
mean_s1s1 = mean(s1s1_intervals);
SD1 = sqrt(var(s1s1_intervals(1:end-1) - s1s1_intervals(2:end)) / 2);
SD2 = sqrt(2 * var(s1s1_intervals) - SD1^2);
SDNN = std(s1s1_intervals);

NN50 = sum(abs(diff_s1s1_intervals) > 50/1000);
pNN50 = NN50 / length(s1s1_intervals) * 100;

% Calculate RMSSD
RMSSD = sqrt(mean(diff_s1s1_intervals .^ 2));

% Calculate Triangular Index (Histogram Width)
[hist_counts, hist_edges] = histcounts(s1s1_intervals, 'BinWidth', 1); % Adjust BinWidth as needed
Triangular_Index = length(s1s1_intervals) / max(hist_counts);

% Calculate HRV Index
HRV_Index = length(s1s1_intervals) / max(hist_counts);

% For poincare plot
% Calculate area of the ellipse S
S = pi * SD1 * SD2;

% Calculate the ratio SD1/SD2
SD1_SD2_ratio = SD1 / SD2;

% Display results
fprintf('SDNN: %.2f s\n', SDNN);
fprintf('NN50: %d\n', NN50);
fprintf('pNN50: %.2f%%\n', pNN50);
fprintf('RMSSD: %.2f s\n', RMSSD);
fprintf('Triangular Index: %.2f\n', Triangular_Index);
fprintf('HRV Index: %.2f\n\n', HRV_Index);
```

This MATLAB script calculates several Heart Rate Variability (HRV) metrics from S1-S1 intervals
(usually people use RR peak, an ecg measurement), which 
represent the time between successive S1 heart sounds. The script also generates several visualization including

- The time series plot of S1-S1 intervals 
- Histogram of the interval distribution with a fitted normal curve
- Poincaré plot 

Then from S1-S1 you can basically calculate whatever metric you want.
The hard part is actually the definition(ie. what does this metric even mean?), so here is the list of calculated metrics and their definition:


```{note}
```{list-table} HRV Time Variable
:header-rows: 1
*   - Term
    - Definition
*   - S1-S1 Intervals (s1s1_intervals)
    - The time differences between consecutive S1 heart sounds
*   - Difference in S1-S1 Intervals (diff_s1s1_intervals)
    - The difference between consecutive S1-S1 intervals.
*   - Mean S1-S1 Interval (mean_s1s1)
    - The average duration of the S1-S1 intervals
*   - Standard Deviation 1 (SD1) (SD1)
    - Reflects the short-term variability of the S1-S1 intervals
*   - Standard Deviation 2 (SD2) (SD2)
    - Reflects the long-term variability of the S1-S1 intervals
*   - Standard Deviation of Normal-to-Normal Intervals (SDNN) (SDNN)
    - A global HRV measure representing the overall variability
*   - NN50 (NN50)
    - The number of successive interval differences greater than 50 ms
*   - pNN50 (pNN50)
    - The proportion of NN50 to the total number of intervals, expressed as a percentage
*   - Root Mean Square of Successive Differences (RMSSD) (RMSSD)
    - A measure of short-term HRV
*   - Triangular Index (Triangular_Index)
    - The total number of S1-S1 intervals divided by the height of the histogram's peak
*   - HRV Index (HRV_Index)
    - A similar measure to the Triangular Index, representing the variability distribution
```

Then the rest of the code is for plotting 
```matlab
% Time vector for S1-S1 intervals (you can create one if not available)
S1_time = cumsum(s1s1_intervals);

% 1. Time Series Plot of S1-S1 Intervals
figure;
plot(S1_time, s1s1_intervals, '-o');
xlabel('Time (s)');
ylabel('S1-S1 Intervals (s)');
title('Time Series of S1-S1 Intervals');
grid on;

% Histogram with fitted normal distribution
figure;
histfit(s1s1_intervals * 1000, 50); % Convert to milliseconds for display
hold on;
xlabel('S1-S1 Intervals (ms)');
ylabel('Frequency')
title('Distribution of S1-S1 Intervals');
legend('Histogram', 'Fitted Normal Distribution')
grid on


time_color = linspace(min(S1_time(1:end-1)), max(S1_time(1:end-1)), length(s1s1_intervals) - 1);

figure;
scatter(s1s1_intervals(1:end-1)-mean_s1s1, s1s1_intervals(2:end)-mean_s1s1, 10, time_color, 'filled');
xlabel('S1-S1 Interval (n) (s)');
ylabel('S1-S1 Interval (n+1) (s)');
title('Poincare Plot');
grid on;
cbar = colorbar;
colormap(parula);
caxis([min(time_color), max(time_color)]);
ylabel(cbar, 'Time (s)');

% Ellipse fitting for Poincare Plot
hold on;
theta = linspace(0, 2*pi, 100);
theta_angle = atan2(SD2, SD2);
ellipse_x = SD1 * cos(theta);
ellipse_y = SD2 * sin(theta);
R = [cos(theta_angle), -sin(theta_angle); sin(theta_angle), cos(theta_angle)];
ellipse_coords = R * [ellipse_x; ellipse_y];
% ellipse_coords = [ellipse_x'; ellipse_y'];
plot(ellipse_coords(1,:), ellipse_coords(2,:), 'k', LineWidth=2);
show_std_diff = 10;
xlim([- show_std_diff*SD1, show_std_diff*SD1]);
ylim([- show_std_diff*SD2, show_std_diff*SD2]);
% Add arrows for SD1 and SD2
line_length = 10;
quiver(0, 0, line_length*SD1*cos(theta_angle), line_length*SD1*sin(theta_angle), 0, 'r', LineWidth= 1, MaxHeadSize=0.1); % SD1 line
quiver(0, 0, line_length*-SD1*cos(theta_angle), line_length*-SD1*sin(theta_angle), 0, 'r', LineWidth=1, MaxHeadSize=0); % SD1 opposite line

quiver(0, 0, line_length*SD2*cos(theta_angle+pi/2), line_length*SD2*sin(theta_angle+pi/2), 0, 'k', LineWidth=1, MaxHeadSize=0.1); % SD2 line
quiver(0, 0, line_length*-SD2*cos(theta_angle+pi/2), line_length*-SD2*sin(theta_angle+pi/2), 0, 'k', LineWidth=1, MaxHeadSize=0); % SD2 opposite line

% Add lines for SD1 and SD2
legend('S1-S1', '95% interval', 'SD1', '', 'SD2')
hold off;
```
## HRV Frequency Domain Calculation
```matlab
Fs = 4; % Sampling frequency for pwelch

% Create a time vector for interpolation
time_vector = all_locs(1):1/Fs:all_locs(end);

% Perform cubic spline interpolation to resample the S1_S1 intervals
s1s1_interpolated = interp1(all_locs(1:end-1), s1s1_intervals, time_vector, 'spline');

% Detrend the signal (optional)
s1s1_detrended = detrend(s1s1_interpolated);

[PSD, f] = pwelch(s1s1_detrended, [], [], [], Fs);

% Define the frequency bands
VLF_band = [0.0033 0.04]; % VLF band (0.0033-0.04 Hz)
LF_band = [0.04 0.15];    % LF band (0.04-0.15 Hz)
HF_band = [0.15 0.4];     % HF band (0.15-0.4 Hz)

% Calculate the power in each band using the bandpower function
VLF_power = bandpower(PSD, f, VLF_band, 'psd');
LF_power = bandpower(PSD, f, LF_band, 'psd');
HF_power = bandpower(PSD, f, HF_band, 'psd');
```
Finally it's the frequency domain calculation.
For frequency domain, we mainly look at 3 band:
1. Very Low Frequency (VLF): 0.0033 to 0.04 Hz 
2. Low Frequency (LF): 0.04 to 0.15 Hz
3. High Frequency (HF): 0.15 to 0.4 Hz
As a brief summary of each component, here is what each frequency band tries to quantify:
```{note}
```{list-table}
:header-rows: 1
*   - Term
    - Definition
*   - VLF
    - Reflects slow regulatory mechanisms, possibly related to sympathetic activity, but its interpretation is complex
*   - LF
    - Reflects a combination of sympathetic and parasympathetic influences
*   - HF
    - Reflects parasympathetic activity, closely tied to respiratory influences
```
In the literature, people actually isn't sure about the legitimacy of VLF, but I included it in the code for completion sake anyway.
The rest of the code is just plotting

```matlab
% Plot the PSD in s²/Hz
figure;
plot(f, PSD, 'k'); % Plot in linear scale with black line
xlabel('Frequency (Hz)');
ylabel('Power/Frequency (s^2/Hz)');
title('Power Spectral Density of S1-S1 Intervals');
grid on;
hold on;

% Shade VLF band under the curve
area(f(f >= VLF_band(1) & f <= VLF_band(2)), PSD(f >= VLF_band(1) & f <= VLF_band(2)), ...
    'FaceColor', 'green', 'FaceAlpha', 0.3);

% Shade LF band under the curve
area(f(f >= LF_band(1) & f <= LF_band(2)), PSD(f >= LF_band(1) & f <= LF_band(2)), ...
    'FaceColor', 'blue', 'FaceAlpha', 0.3);

% Shade HF band under the curve
area(f(f >= HF_band(1) & f <= HF_band(2)), PSD(f >= HF_band(1) & f <= HF_band(2)), ...
    'FaceColor', 'red', 'FaceAlpha', 0.3);

legend('PSD', 'VLF Band', 'LF Band', 'HF Band');
xlim([0 0.5])
% Print the band powers
fprintf('VLF Power: %f\n', VLF_power);
fprintf('LF Power: %f\n', LF_power);
fprintf('HF Power: %f\n', HF_power);
```
Congratulations, this is the most jam packed matlab script in this project, thanks for reading this :D
## Full Code
```matlab
clear; close all; clc;
[audio, fs] = audioread('audio/240729_sleep_modified.wav');
downsampling_const = 10;
audio = downsample(audio, downsampling_const);
fs = fs/downsampling_const;
if size(audio, 2) == 2
    audio = mean(audio, 2);
end

max_time = length(audio)/fs;
time = 0:1/fs:max_time - 1/fs;

cutoffFreq = 60;
segmentTimeWindow = 5;
secondOverlap = 1;

[timeVec, filtered_audio] = butterworthFilter(audio, cutoffFreq, fs, max_time);
filtered_audio = medfilt1(filtered_audio,25);
filtered_audio = filtered_audio / max(abs(filtered_audio));

% Detect peaks
envelope = abs(hilbert(filtered_audio));
edge_compensation = 10;
envelope = envelope(1:length(envelope)-edge_compensation);

local_amplitude_window = 10;
peak_window_size = local_amplitude_window * fs; 
peak_step_size = local_amplitude_window * fs; %decided that no overlap is better
all_peaks = [];
all_locs = [];
num_peak_windows = floor((length(envelope) - peak_window_size) / peak_step_size) + 1;


for j = 1:num_peak_windows
    peak_start_idx = (j-1) * peak_step_size + 1;
    peak_end_idx = peak_start_idx + peak_window_size - 1;
    if peak_end_idx > length(envelope)
        peak_end_idx = length(envelope);
    end
    envelope_window = envelope(peak_start_idx:peak_end_idx);
    local_max = max(envelope_window);
    [window_peaks, window_locs] = findpeaks(envelope_window, MinPeakHeight = local_max * 0.6, MinPeakDistance = fs * 0.5);
    all_peaks = [all_peaks; window_peaks];
    all_locs = [all_locs; window_locs + peak_start_idx - 1];
end

window_size = 10 * fs; 
step_size = 0.5 * fs;
num_windows = floor((length(audio) - window_size) / step_size) + 1;

time_stamps = zeros(num_windows, 1);
bpm_values = zeros(num_windows, 1);

for i = 1:num_windows
    start_idx = (i-1) * step_size + 1;
    end_idx = start_idx + window_size - 1;
    window_peaks = all_locs(all_locs >= start_idx & all_locs <= end_idx);
    
    if length(window_peaks) > 1
        intervals = diff(window_peaks) / fs;
        avg_interval = mean(intervals);
        bpm_values(i) = 60 / avg_interval;
    else
        bpm_values(i) = NaN; % Not enough peaks to calculate BPM
    end
    
    time_stamps(i) = (start_idx + end_idx) / (2 * fs); % Midpoint of the window
end

%fig to check whether algorithm works or not
all_locs = all_locs/fs;
% Plot
figure;
subplot(211)
plot(time_stamps, bpm_values, '-o');
xlabel('Time (seconds)');
ylabel('BPM');
title('Time vs Heartbeat Per Minute');
ylim([40 160])
zoom xon
% xlim([0 300])
grid on;
% xlim([0,120])
% ylim([40, 80])
subplot(212)
plot(time, audio)
xlabel('Time (seconds');
ylabel('Amplitude')
xlim([0,120])
ylim([-1,1])
zoom xon
% plot(,filtered_audio)

fprintf('Heartbeat per minute: %.2f BPM\n', mean(bpm_values, 'omitnan'));
figure;
subplot(311);
plot(time, audio);
title('Original Audio');
zoom xon
% xlim([320 440])
subplot(312);
plot(timeVec, filtered_audio);
title('Filtered Audio');
% xlim([320 440])
subplot(313);
plot(timeVec(1:length(timeVec)-edge_compensation), envelope);
hold on;
plot(all_locs, all_peaks, 'ro');
zoom xon
% xlim([0 120])
% xlim([320 440])
title('Envelope with Detected Peaks');

function [timeVec, filteredSound] = butterworthFilter(audioVec, cutoffFreq, originalFs, max_time)
    % Parameters:
    % audioVec           - Input audio vector
    % cutoffFreq         - Cutoff frequency for low-pass filter
    % downsamplingConst  - Downsampling factor
    % segmentTimeWindow  - Length of each segment time window in seconds
    % secondOverlap      - Overlap time in seconds

    % Sampling frequency of the original signal (assumed)
    % New sampling frequency after downsampling
    
    % Design the Butterworth filter
    [b, a] = butter(5, cutoffFreq / (originalFs / 2), 'low');
    
    % Apply the Butterworth filter
    filteredSound = filtfilt(b, a, audioVec);
    fprintf("filtered sound size: %s\n", mat2str(size(filteredSound)));
    % Downsample the filtered signal
    timeVec = linspace(0, max_time,originalFs*max_time);
end
%% 
s1s1_intervals = diff(all_locs);
diff_s1s1_intervals = diff(s1s1_intervals);
mean_s1s1 = mean(s1s1_intervals);
SD1 = sqrt(var(s1s1_intervals(1:end-1) - s1s1_intervals(2:end)) / 2);
SD2 = sqrt(2 * var(s1s1_intervals) - SD1^2);
SDNN = std(s1s1_intervals);

NN50 = sum(abs(diff_s1s1_intervals) > 50/1000);
pNN50 = NN50 / length(s1s1_intervals) * 100;

% Calculate RMSSD
RMSSD = sqrt(mean(diff_s1s1_intervals .^ 2));

% Calculate Triangular Index (Histogram Width)
[hist_counts, hist_edges] = histcounts(s1s1_intervals, 'BinWidth', 1); % Adjust BinWidth as needed
Triangular_Index = length(s1s1_intervals) / max(hist_counts);

% Calculate HRV Index
HRV_Index = length(s1s1_intervals) / max(hist_counts);

% For poincare plot
% Calculate area of the ellipse S
S = pi * SD1 * SD2;

% Calculate the ratio SD1/SD2
SD1_SD2_ratio = SD1 / SD2;

% Display results
fprintf('SDNN: %.2f s\n', SDNN);
fprintf('NN50: %d\n', NN50);
fprintf('pNN50: %.2f%%\n', pNN50);
fprintf('RMSSD: %.2f s\n', RMSSD);
fprintf('Triangular Index: %.2f\n', Triangular_Index);
fprintf('HRV Index: %.2f\n\n', HRV_Index);

% Time vector for S1-S1 intervals (you can create one if not available)
S1_time = cumsum(s1s1_intervals);

% 1. Time Series Plot of S1-S1 Intervals
figure;
plot(S1_time, s1s1_intervals, '-o');
xlabel('Time (s)');
ylabel('S1-S1 Intervals (s)');
title('Time Series of S1-S1 Intervals');
grid on;

% Histogram with fitted normal distribution
figure;
histfit(s1s1_intervals * 1000, 50); % Convert to milliseconds for display
hold on;
xlabel('S1-S1 Intervals (ms)');
ylabel('Frequency')
title('Distribution of S1-S1 Intervals');
legend('Histogram', 'Fitted Normal Distribution')
grid on


time_color = linspace(min(S1_time(1:end-1)), max(S1_time(1:end-1)), length(s1s1_intervals) - 1);

figure;
scatter(s1s1_intervals(1:end-1)-mean_s1s1, s1s1_intervals(2:end)-mean_s1s1, 10, time_color, 'filled');
xlabel('S1-S1 Interval (n) (s)');
ylabel('S1-S1 Interval (n+1) (s)');
title('Poincare Plot');
grid on;
cbar = colorbar;
colormap(parula);
caxis([min(time_color), max(time_color)]);
ylabel(cbar, 'Time (s)');

% SD2 = 1;
% SD1 = 2;
% Ellipse fitting for Poincare Plot
hold on;
theta = linspace(0, 2*pi, 100);
theta_angle = atan2(SD2, SD2);
ellipse_x = SD1 * cos(theta);
ellipse_y = SD2 * sin(theta);
R = [cos(theta_angle), -sin(theta_angle); sin(theta_angle), cos(theta_angle)];
ellipse_coords = R * [ellipse_x; ellipse_y];
% ellipse_coords = [ellipse_x'; ellipse_y'];
plot(ellipse_coords(1,:), ellipse_coords(2,:), 'k', LineWidth=2);
show_std_diff = 10;
xlim([- show_std_diff*SD1, show_std_diff*SD1]);
ylim([- show_std_diff*SD2, show_std_diff*SD2]);
% Add arrows for SD1 and SD2
line_length = 10;
quiver(0, 0, line_length*SD1*cos(theta_angle), line_length*SD1*sin(theta_angle), 0, 'r', LineWidth= 1, MaxHeadSize=0.1); % SD1 line
quiver(0, 0, line_length*-SD1*cos(theta_angle), line_length*-SD1*sin(theta_angle), 0, 'r', LineWidth=1, MaxHeadSize=0); % SD1 opposite line

quiver(0, 0, line_length*SD2*cos(theta_angle+pi/2), line_length*SD2*sin(theta_angle+pi/2), 0, 'k', LineWidth=1, MaxHeadSize=0.1); % SD2 line
quiver(0, 0, line_length*-SD2*cos(theta_angle+pi/2), line_length*-SD2*sin(theta_angle+pi/2), 0, 'k', LineWidth=1, MaxHeadSize=0); % SD2 opposite line

% Add lines for SD1 and SD2
legend('S1-S1', '95% interval', 'SD1', '', 'SD2')
hold off;
%%
S1_S1_intervals = diff(all_locs); % Calculate the differences between consecutive peaks
% Define the desired sampling frequency (in Hz)
Fs = 4; % This is a common choice, but you can adjust as needed

% Create a time vector for interpolation
time_vector = all_locs(1):1/Fs:all_locs(end);

% Perform cubic spline interpolation to resample the S1_S1 intervals
S1_S1_interpolated = interp1(all_locs(1:end-1), S1_S1_intervals, time_vector, 'spline');

% Detrend the signal (optional)
S1_S1_detrended = detrend(S1_S1_interpolated);

[PSD, f] = pwelch(S1_S1_detrended, [], [], [], Fs);

% Define the frequency bands
VLF_band = [0.0033 0.04]; % VLF band (0.0033-0.04 Hz)
LF_band = [0.04 0.15];    % LF band (0.04-0.15 Hz)
HF_band = [0.15 0.4];     % HF band (0.15-0.4 Hz)

% Calculate the power in each band using the bandpower function
VLF_power = bandpower(PSD, f, VLF_band, 'psd');
LF_power = bandpower(PSD, f, LF_band, 'psd');
HF_power = bandpower(PSD, f, HF_band, 'psd');

% Plot the PSD in s²/Hz
figure;
plot(f, PSD, 'k'); % Plot in linear scale with black line
xlabel('Frequency (Hz)');
ylabel('Power/Frequency (s^2/Hz)');
title('Power Spectral Density of S1-S1 Intervals');
grid on;
hold on;

% Shade VLF band under the curve
area(f(f >= VLF_band(1) & f <= VLF_band(2)), PSD(f >= VLF_band(1) & f <= VLF_band(2)), ...
    'FaceColor', 'green', 'FaceAlpha', 0.3);

% Shade LF band under the curve
area(f(f >= LF_band(1) & f <= LF_band(2)), PSD(f >= LF_band(1) & f <= LF_band(2)), ...
    'FaceColor', 'blue', 'FaceAlpha', 0.3);

% Shade HF band under the curve
area(f(f >= HF_band(1) & f <= HF_band(2)), PSD(f >= HF_band(1) & f <= HF_band(2)), ...
    'FaceColor', 'red', 'FaceAlpha', 0.3);

legend('PSD', 'VLF Band', 'LF Band', 'HF Band');
xlim([0 0.5])
% Print the band powers
fprintf('VLF Power: %f\n', VLF_power);
fprintf('LF Power: %f\n', LF_power);
fprintf('HF Power: %f\n', HF_power);
```
