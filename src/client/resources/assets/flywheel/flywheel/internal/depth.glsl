float linearize_depth(float d, float zNear, float zFar) {
    float z_n = 1.0 - 2.0 * d;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}

float delinearize_depth(float linearDepth, float zNear, float zFar) {
    float z_n = (2.0 * zNear * zFar / linearDepth) - (zFar + zNear);
    return 0.5 * (1.0 - z_n / (zNear - zFar));
}
