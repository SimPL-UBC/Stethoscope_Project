# nmf_comparison.m
This code just compares the before and after nmf plot, mainly by just passing the two wav file before and after
processing with nmf. The main reason to use this script is to zoom in into the plot and see visually how nmf
change the signal

## Full Code
```matlab
clear; clc; close all

[heartbeat_sound, heartbeat_fs] = audioread('audio/heartbeat_3.wav');
[original_sound, original_fs] = audioread('talk_heartbeat.wav');

time_heartbeat = 0:1/heartbeat_fs:length(heartbeat_sound)/heartbeat_fs-1/heartbeat_fs;
time_original = 0:1/original_fs:length(original_sound)/original_fs-1/original_fs;

main_fig = figure;
subplot(3,3,[1 2 3]); % Main plot in the first row spanning all three columns
plot(time_original, original_sound, 'r')
hold on
plot(time_heartbeat, heartbeat_sound, 'b')
xlim([0 120])
title('Original vs NMF Separated Waveform')
ylabel('Amplitude')
xlabel('Time (s)')

for i = 1:6
    % Define the x-axis limits for each zoomed-in subplot
    x_start = (i-1)*20;
    x_end = i*20;
    % Create a subplot for each zoomed-in section
    subplot(3, 3, 3+i); % Adjusting the subplot positions
    plot(time_original, original_sound, 'r');
    hold on;
    plot(time_heartbeat, heartbeat_sound, 'b');
    xlim([x_start, x_end]);
    title(['Zoomed-In: ', num2str(x_start), ' to ', num2str(x_end), ' seconds']);
    xlabel('Time (s)');
    ylabel('Amplitude');
    
    % Remove x-axis labels for zoomed-in plots (if needed)
    % if row == 2
    %     set(gca, 'XTickLabel', []);
    % end
end
```
