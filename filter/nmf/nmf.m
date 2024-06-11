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

% Step 3: Preprocess the Data
% Convert to mono if the signal is stereo
if size(mixedSignal, 2) == 2
    mixedSignal = mean(mixedSignal, 2);
end

% Convert the signal to a spectrogram
windowLength = 1024;
overlap = 512;
[S, F, T] = spectrogram(mixedSignal, windowLength, overlap, windowLength, fs);

% Step 4: Apply NMF
numComponents = 3; % We want to extract 3 components
[V, W, H] = nnmf(abs(S), numComponents);

% Step 5: Reconstruct the Components
components = cell(1, numComponents);
for i = 1:numComponents
    Si = W(:, i) * H(i, :);
    reconstructedS = Si .* exp(1i * angle(S)); % Combine magnitude with original phase
    componentSignal = istft(reconstructedS, fs, 'Window', hamming(windowLength), 'OverlapLength', overlap, 'FFTLength', windowLength);
    components{i} = real(componentSignal(1:length(mixedSignal))); % Adjust the length
end

% Save the extracted components to separate files
for i = 1:numComponents
    audiowrite(['component_' num2str(i) '.wav'], components{i}, fs);
end

% Visualize the separated components
for i = 1:numComponents
    figure;
    spectrogram(components{i}, windowLength, overlap, [], fs, 'yaxis');
    title(['Component ' num2str(i)]);
end

% Optional: Listen to the separated components
for i = 1:numComponents
    sound(components{i}, fs);
    pause(length(components{i}) / fs + 1);
end
