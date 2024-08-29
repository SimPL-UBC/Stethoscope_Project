# spectral_entropy.m

This script calculates the spectral entropy of the signal, by using the definition of spectral entropy,
which is given by the math equation:

```{math}
:label: eq-label

H = - \Sigma_{i=1}^{N}P_i \log_2{P_i}
```
That is actually the shannon entropy, I can't explain it here in the documentation, but there is
an excellent youtube video about this, [linked here](https://www.youtube.com/watch?v=0GCGaw0QOhA).

Basically, we use the power spectral density, as it can be used to represent probability function
(which makes sense, higher magnitude means you have more chance of seeing that specific frequency)

the main block of code you need to focus is here
```matlab
nfft = length(leftChannel);     % Length of FFT
Px = abs(fft(leftChannel, nfft)).^2; % Power of each frequency component
Px = Px(1:nfft/2+1); % Remember FFT is symmetric

f = (0:nfft/2)*fs/nfft; % Frequency vector

% Step 2: Normalize the PSD to get a probability distribution
Px_norm = Px / sum(Px); % Normalize the power

% Step 3: Compute the spectral entropy
spec_entropy_x = -sum(Px_norm .* log2(Px_norm)); % Entropy
```

## Full Code
```matlab
clear; clc; close all;
filename = "audio/15min_HB.wav";
[data, fs] = audioread(filename);
data = data(70*fs:110*fs,:);
leftChannel = data(:, 1);
rightChannel = data(:, 2);

t_wave = (0:length(leftChannel)-1) / fs;

subplot(211);
plot(t_wave, leftChannel);
title('Left Channel Waveform');
xlabel('Time (s)');
ylabel('Amplitude');
grid on;

nfft = length(leftChannel);     % Length of FFT
Px = abs(fft(leftChannel, nfft)).^2; % Power of each frequency component
Px = Px(1:nfft/2+1); % Remember FFT is symmetric

f = (0:nfft/2)*fs/nfft; % Frequency vector

% Step 2: Normalize the PSD to get a probability distribution
Px_norm = Px / sum(Px); % Normalize the power

% Step 3: Compute the spectral entropy
spec_entropy_x = -sum(Px_norm .* log2(Px_norm)); % Entropy

% Display the result
fprintf('Spectral Entropy: %f bits\n', spec_entropy_x);

% Plot the original PSD
subplot(212);
plot(f, Px);
title('Power Spectral Density (PSD)');
xlabel('Frequency (Hz)');
ylabel('Power');
xlim([0 1000])
```
