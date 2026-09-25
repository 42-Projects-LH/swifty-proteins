package com.example.swiftyproteins.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RcsbApiService {

    @GET("ligands/view/{ligand}.cif")
    suspend fun getLigandCif(@Path("ligand") ligandId: String): Response
}
