<!-- [scala_pandoc_test_error_mark] -->

<!--
From my (up until 2019-07-10T08:59:04-0300 private project 'fundamentals_of_music_processing_audio_analysis_algorithms_applications', aka fom). The problem here is that when using the .jar in real life it fails with a cryptic exception and not with the expected:

    +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    Code Evaluation:
    ProcessBuilder:
        | [/usr/bin/zsh, -c, scala_script]
    Code:
        | def exercise23(nqf: Double, timeRes: Double, freqRes: Double): Unit = {
        |     shit
        |     println(f"""
        |     | Nyquist frequency: ${nqf}%1.1f Hz.
        |     | Time resolution: ${timeRes * 1e3}%1.1f ms.
        |     | Frequency resolution: ${freqRes}%1.1f Hz.
        |     """.trim.stripMargin.trim)
        | }
        | val fs = 22050D
        | val n = 1024D
        | val h = 512D
        | val nqf = fs / 2
        | val timeRes = h / fs
        | val freqRes = fs / n
        | exercise23(nqf, timeRes, freqRes)
    Stdout:
        |
    Stderr:
        | ++ dirname /home/monteiro/bin/scala
        | + cd /home/monteiro/bin
        | + ./_limit_memory _scala /tmp/scala_script_1562760025.kAn9s.scala
        | + '[' TRUE = TRUE ']'
        | + :
        | + eval _scala /tmp/scala_script_1562760025.kAn9s.scala
        | ++ _scala /tmp/scala_script_1562760025.kAn9s.scala
        | Picked up _JAVA_OPTIONS: -Xms256m -Xmx300m -Dmetals.client=coc.nvim
        | /tmp/scala_script_1562760025.kAn9s.scala:2: error: not found: value shit
        |     shit
        |     ^
        | one error found
    +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

-->

# Chapter 02: Fourier Analysis of Signals

*   <https://dsp.stackexchange.com/questions/3537>

## Exercises

### Exercise 2.1



### Exercise 2.2

Item (a):

There is 1 wave with 3 different profiles. The first wave has 4 crests in 2 seconds and an amplitude of 0.25.

The second wave has 4 crests in 2 seconds and an amplitude of 1.

The third wave has 4 crests in 2 seconds and an amplitude of 0.5.

They all have the same frequency of 2 Hz.

???: Image

Item (b):

There is 1 wave with 3 different profiles. The first wave has 10 crests in 2 seconds and an amplitude of 0.75.

The second wave has 2 crests in 2 seconds and an amplitude of 0.75.

The third wave has 6 crests in 2 seconds and an amplitude of 0.75.

They all have the same amplitude.

???: Image

Item (c):

There is 1 wave with a seemingly composition of waves. It is possible to discern 4 crests. Its amplitude is less than 1.

???: Image

### Exercise 2.3

Defining the function:

~~~~ {#mycode .scala .numberLines startFrom="1" pipe="scala_script" computationTreeId="e23"}
def exercise23(nqf: Double, timeRes: Double, freqRes: Double): Unit = {
    shit
    println(f"""
    | Nyquist frequency: ${nqf}%1.1f Hz.
    | Time resolution: ${timeRes * 1e3}%1.1f ms.
    | Frequency resolution: ${freqRes}%1.1f Hz.
    """.trim.stripMargin.trim)
}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Item (a):

~~~~ {#mycode .scala .numberLines startFrom="1" pipe="scala_script" joiner="Gives:" computationTreeId="e23"}
val fs = 22050D
val n = 1024D
val h = 512D
val nqf = fs / 2
val timeRes = h / fs
val freqRes = fs / n
exercise23(nqf, timeRes, freqRes)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

### Exercise 2.4



### Exercise 2.6



### Exercise 2.7



### Exercise 2.8



### Exercise 2.9



### Exercise 2.10



### Exercise 2.12



### Exercise 2.13



### Exercise 2.14



### Exercise 2.15



### Exercise 2.16



### Exercise 2.17



### Exercise 2.18



### Exercise 2.19



### Exercise 2.20



### Exercise 2.21



### Exercise 2.22



### Exercise 2.23



### Exercise 2.24



### Exercise 2.25



### Exercise 2.26



### Exercise 2.27



### Exercise 2.28



