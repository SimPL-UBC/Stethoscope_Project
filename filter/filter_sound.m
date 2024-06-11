clear; close all; clc;
% Step 1: Use uigetfile to select the mixed audio file
[file, path] = uigetfile('*.wav', 'Select the Mixed Audio File');
if isequal(file, 0)
    disp('Please select a file');
    return;
else
    filename = fullfile(path, file);
end

% Step 2: Load the selected audio file
[mixed_audio, fs] = audioread(filename);

% Step 3: Define the filters for each sound type

% Define the heartbeat filter
heartbeat_filter = designfilt('bandpassiir', 'FilterOrder', 8, ...
                              'HalfPowerFrequency1', 20, 'HalfPowerFrequency2', 150, ...
                              'SampleRate', fs);

% Define the lung filter
lung_filter = designfilt('bandpassiir', 'FilterOrder', 8, ...
                         'HalfPowerFrequency1', 200, 'HalfPowerFrequency2', 600, ...
                         'SampleRate', fs);

% Define the conversation filter
conversation_filter = designfilt('bandpassiir', 'FilterOrder', 8, ...
                                 'HalfPowerFrequency1', 300, 'HalfPowerFrequency2', 3400, ...
                                 'SampleRate', fs);

% Step 4: Apply the filters to the mixed audio
filtered_heartbeat = filtfilt(heartbeat_filter, mixed_audio);
filtered_lung = filtfilt(lung_filter, mixed_audio);
filtered_conversation = filtfilt(conversation_filter, mixed_audio);

% Step 5: Plot the waveforms
figure;

% Plot the original mixed audio
subplot(4, 1, 1);
plot((1:length(mixed_audio))/fs, mixed_audio);
title('Original Mixed Audio');
xlabel('Time (s)');
ylabel('Amplitude');

% Plot the filtered heartbeat audio
subplot(4, 1, 2);
plot((1:length(filtered_heartbeat))/fs, filtered_heartbeat);
title('Filtered Heartbeat Audio');
xlabel('Time (s)');
ylabel('Amplitude');

% Plot the filtered lung audio
subplot(4, 1, 3);
plot((1:length(filtered_lung))/fs, filtered_lung);
title('Filtered Lung Audio');
xlabel('Time (s)');
ylabel('Amplitude');

% Plot the filtered conversation audio
subplot(4, 1, 4);
plot((1:length(filtered_conversation))/fs, filtered_conversation);
title('Filtered Conversation Audio');
xlabel('Time (s)');
ylabel('Amplitude');

% Step 6: Save the filtered audio components
audiowrite('filtered_heartbeat.wav', filtered_heartbeat, fs);
audiowrite('filtered_lung.wav', filtered_lung, fs);
audiowrite('filtered_conversation.wav', filtered_conversation, fs);

disp('Filtered audio files saved successfully.');