# NMF.ipynb

Non-Negative Matrix Factorization (NMF) is the factorization technique we
used to get certain sound feature. Turns out you can easily distinguish repeating
sound feature using algorithm like NMF.
The state of the art is actually a deep learning model, but I don't have
the required knowledge to do deep learning, so I settled for more 
traditional signal processing algorithm.

NMF is a blind source separation algorithm, which means that you don't
need to know about the input to separate it to multiple components.
The way they do this is by approximating the original sound (matrix).
If you know SVD (Singular Value Decomposition) you basically know everything
about this algorithm, it basically uses sum of rank-1 matrices. I won't
be explaining too too much in this documentation, but I will provide a link
for any of you that are interested in this algorithm.

The algorithm I use isn't created by me though, credit to [Zahra Benslimane](https://medium.com/@zahrahafida.benslimane/audio-source-separation-using-non-negative-matrix-factorization-nmf-a8b204490c7d) for this.
Her blog also explain a little bit of the implemented algorithm. Finally here is a few resources for you
to learn about NMF:
- [A quick overview on feature selection](https://www.sciencedirect.com/science/article/pii/B9780323859554000041#s0130)
- [NMF explanation](https://blog.acolyer.org/2019/02/18/the-why-and-how-of-nonnegative-matrix-factorization/)
- [NMF Algorithm](https://www.youtube.com/watch?v=vc-WlAqv17c)
- [Minimum Value Constrain](https://www.youtube.com/watch?v=pLtJpbiK9fc)

## How to Use

1. To use the algorithm, open the `Audio-Source-Separation-using-NMF.ipynb`.
2. Install all the dependencise (mainly librosa)
3. Select your audio file in the `audio_file` variable
4. Choose a sample rate, usually I downsample the signal because it will take too too much ram otherwise
5. On header 3, first cell there is a variable called `S` there, this specify how many feature you can get. You can just specify a large number for this if you want to
6. Also you can modify the `beta` variable, which is your loss function, basically a family of probability distribution. `Beta = 2` is your usual distance squared, other common choice of `Beta` is 1 and 0
7. Run all except the save file cell
8. Just wait for the algorithm to finish, it takes quite a while
9. Once it's finished, there will be displayed sound and spectogram of the separated sound sources
10. Hear which sound you want to extract, keeping note of the indices
11. Put your indices on a variable such as `heartbeat_index`
12. Splice and combine your sound signal
