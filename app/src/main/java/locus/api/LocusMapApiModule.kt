package locus.api

import locus.api.manager.LocusMapManager
import locus.api.mapper.DataMapper
import locus.api.mapper.GeocacheConverter
import locus.api.mapper.GeocacheLogConverter
import locus.api.mapper.ImageDataConverter
import locus.api.mapper.PointMerger
import locus.api.mapper.TrackableConverter
import locus.api.mapper.WaypointConverter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Created by Arcao on 08.12.2018.
 */
internal val locusMapApiModule = module {
    single { LocusMapManager(androidContext()) }
    single { DataMapper(get(), get(), get()) }
    single { GeocacheConverter(androidContext(), get(), get(), get(), get(), get()) }
    single { GeocacheLogConverter(get()) }
    single { ImageDataConverter() }
    single { PointMerger(get()) }
    single { TrackableConverter() }
    single { WaypointConverter() }
}
