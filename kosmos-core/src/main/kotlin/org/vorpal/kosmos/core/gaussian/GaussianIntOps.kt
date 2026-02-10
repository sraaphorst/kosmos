package org.vorpal.kosmos.core.gaussian

import org.vorpal.kosmos.core.rational.toRational

fun GaussianInt.toGaussianRat(): GaussianRat =
    GaussianRat(re.toRational(), im.toRational())
