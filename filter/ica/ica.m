clear; close all; clc;

% Step 1: Use uigetfile to select the mixed audio file
[file, path] = uigetfile('*.wav', 'Select the Mixed Audio File');
if isequal(file, 0)
    disp('Please select a file');
    return;
else
    filename = fullfile(path, file);
end

% Step 2: Load the Mixed Sound File
[mixedSignal, fs] = audioread(filename);

% Step 3: Preprocess the Data (if necessary)
% For example, convert to mono if the signal is stereo
if size(mixedSignal, 2) == 2
    mixedSignal = mean(mixedSignal, 2);
end

% Visualize the mixed signal
figure;
plot(mixedSignal);
title('Mixed Signal');
xlabel('Sample Number');
ylabel('Amplitude');

% Step 4: Apply RICA
% Set parameters for RICA
numComponents = 3; % We want to extract 3 components
maxIterations = 1000;
lambda = 0.0001; % Regularization parameter (adjust this)

% Apply RICA
ricaModel = rica(mixedSignal, numComponents, 'IterationLimit', maxIterations, 'Lambda', lambda);

% Step 5: Extract Components
components = transform(ricaModel, mixedSignal);

% Save the extracted components to separate files
audiowrite('component_heartbeat.wav', components(:, 1), fs);
audiowrite('component_lung_sound.wav', components(:, 2), fs);
audiowrite('component_human_conversation.wav', components(:, 3), fs);

% Visualize the separated components
figure;
subplot(3, 1, 1);
plot(components(:, 1));
title('Heartbeat Component');
xlabel('Sample Number');
ylabel('Amplitude');

subplot(3, 1, 2);
plot(components(:, 2));
title('Lung Sound Component');
xlabel('Sample Number');
ylabel('Amplitude');

subplot(3, 1, 3);
plot(components(:, 3));
title('Human Conversation Component');
xlabel('Sample Number');
ylabel('Amplitude');