<!--
The first block must be shown as is in the pdf.

The second block must be substituted correctly.
-->

~~~~ {#mycode .scala .numberLines startFrom="1" computationTreeId="e23"}
def exercise23(nqf: Double, timeRes: Double, freqRes: Double): Unit = {
    println(f"""
    | Nyquist frequency: ${nqf}%1.1f Hz.
    | Time resolution: ${timeRes * 1e3}%1.1f ms.
    | Frequency resolution: ${freqRes}%1.1f Hz.
    """.trim.stripMargin.trim)
}
println("empty")
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

~~~~ {#mycode .scala .numberLines startFrom="1" pipe="scala_script" joiner="Gives:" computationTreeId="e23"}
val fs = 22050D
val n = 1024D
val h = 512D
val nqf = fs / 2
val timeRes = h / fs
val freqRes = fs / n
exercise23(nqf, timeRes, freqRes)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
